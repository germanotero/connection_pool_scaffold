package com.opower.connectionpool;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.opower.connectionpool.connection.DefaultPooledConnection;
import com.opower.connectionpool.connection.PooledConnectionInvocationHandler;
import com.opower.connectionpool.exceptions.ConnectionPoolException;

/**
 * Implementation of the interface for the Database Connection Pool
 * 
 * @author German Otero
 * 
 */
public class ConnectionPoolImpl implements ConnectionPool {
	private final Logger logger = Logger.getLogger(ConnectionPoolImpl.class);
	private ConnectionPoolDataSourceImpl dataSource;

	private Integer validTimeout = 1000;

	private Integer minPoolSize;
	private Integer maxPoolSize;
	private Integer initialPoolSize;
	// The timeout of a connection that is in the active list that can be with
	// out any command
	private Long abandonedTimeout;
	// The timeout of a connection that is in the idle list
	private Long idleTimeout;

	private volatile int currentPoolSize = 0;

	private final ReentrantLock lock = new ReentrantLock();
	private BlockingQueue<DefaultPooledConnection> activeConnections;
	private BlockingQueue<DefaultPooledConnection> idleConnections;

	public ConnectionPoolImpl(String url, String user, String password, Integer initialPoolSize, Integer minPoolSize,
			Integer maxPoolSize, Long idleTimeout, Long abandonedTimeout) {
		checkParameters(initialPoolSize, minPoolSize, maxPoolSize);

		this.dataSource = new ConnectionPoolDataSourceImpl(url, user, password, new PooledConnectionEventListener(this));

		this.idleTimeout = idleTimeout;
		this.abandonedTimeout = abandonedTimeout;

		this.initialPoolSize = initialPoolSize;
		this.minPoolSize = minPoolSize;
		this.maxPoolSize = maxPoolSize;

		idleConnections = new ArrayBlockingQueue<DefaultPooledConnection>(maxPoolSize);
		activeConnections = new ArrayBlockingQueue<DefaultPooledConnection>(maxPoolSize);
	}

	/**
	 * Checks parameters initialPoolSize, minPoolSize and maxPoolSize to fit in
	 * the valid values
	 */
	private void checkParameters(Integer initialPoolSize, int minPoolSize, int maxPoolSize) {
		if (initialPoolSize < 1) {
			throw new IllegalArgumentException("Initial pool size must be > 1");
		}
		if (initialPoolSize > maxPoolSize) {
			throw new IllegalArgumentException("Initial pool size, can't be higher than Max pool size");
		}
		if (initialPoolSize < minPoolSize) {
			throw new IllegalArgumentException("Initial pool size, can't be lower than Min pool size");
		}

	}

	/**
	 * Starts the pool
	 * 
	 * @throws SQLException
	 *             if an exception ocurrs during the creation of the initial
	 *             pool size
	 */
	public void start() throws SQLException {
		fillIdle(initialPoolSize);
	}

	/**
	 * Returns a connection from the pool
	 */
	public Connection getConnection() throws SQLException {
		DefaultPooledConnection pooledConnection;

		if (currentPoolSize == 0) {
			throw new ConnectionPoolException(
					"Connection pool is empty. Usually this means the pool hasn't been started or all connections in the pool has been taken");
		}
		pooledConnection = this.pollIdleConnection();

		if (pooledConnection == null) {
			if (logger.isDebugEnabled())
				logger.debug("No idle connection found, we will create a new one for you");
			pooledConnection = createConnection();
		} else {
			// cast because I'm 100% sure this is the class and I need the touch
			// method.
			((DefaultPooledConnection) pooledConnection).touch();
			if (!pooledConnection.getConnection().isValid(this.validTimeout)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Trying to return a pooled connection but was invalid");
				}
				return getConnection();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Returning a pooled connection");
			}
		}

		activeConnections.add(pooledConnection);
		return (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { Connection.class }, new PooledConnectionInvocationHandler(pooledConnection));
	}

	/**
	 * Creates a new connection getting it from the datasource, it increments
	 * the currentPoolSize, but it does not store it.
	 */
	private DefaultPooledConnection createConnection() {
		DefaultPooledConnection connection = null;
		lock.lock();
		try {
			if (currentPoolSize < maxPoolSize) {
				connection = getDatasourceConnection();
				currentPoolSize++;
				if (logger.isDebugEnabled())
					logger.debug("Creating a new connection. PoolSize: " + currentPoolSize);
			} else {
				throw new ConnectionPoolException("Can't create a new connection because max pool size reached");
			}
		} finally {
			lock.unlock();
		}
		return connection;
	}

	/**
	 * Release this connection back into the pool, if the connection is not
	 * handled by this pool, the connection will be closed, and warnings will be
	 * logged.
	 * 
	 * @param Connection
	 */
	public void releaseConnection(Connection connection) throws SQLException {
		DefaultPooledConnection pooledConnection = null;
		try {
			pooledConnection = ((PooledConnectionInvocationHandler) Proxy.getInvocationHandler(connection))
					.getPooledConnection();
		} catch (Exception e) {
			logger.warn("Trying to release a non proxy connection..", e);
		}
		if (pooledConnection != null) {
			lock.lock();
			pooledConnection.touch();
			try {
				if (connection.isClosed() || !connection.isValid(this.validTimeout)) {
					this.doRemoveConnection(pooledConnection);
					if (logger.isDebugEnabled()) {
						logger.debug("Releasing a connection that was closed or invalid, it will be removed from the pool");
					}
					// review this
					this.fillIdle(minPoolSize);
				} else {
					activeConnections.remove(pooledConnection);
					idleConnections.add(pooledConnection);
					if (logger.isDebugEnabled()) {
						logger.debug("Releasing a connection, it has been moved to idle");
					}
				}
			} finally {
				lock.unlock();
			}
		} else {
			logger.warn("Attempting to close a connection that no longer or never had this pool, the connection will be closed and not handled by this pool");
			connection.close();
		}
	}

	/**
	 * This method, will execute the process of cleaning the pool, it will look
	 * for inactive connections from the active list, and return them back to
	 * the pool, also it will search for invalid connections and remove them
	 * from the pool. Also refill the pool.
	 */
	void clean() {
		lock.lock();
		try {
			for (DefaultPooledConnection pooledConnection : this.activeConnections) {
				Long inactiveTime = System.currentTimeMillis() - pooledConnection.getLastTouch();
				if ((inactiveTime > this.abandonedTimeout) && !removeIfInvalidOrClosed(pooledConnection)) {
					pooledConnection.releaseFromProxy();
					moveToIdle(pooledConnection);
				}
			}

			for (DefaultPooledConnection connection : this.idleConnections) {
				Long idleTime = System.currentTimeMillis() - connection.getLastTouch();
				removeIfInvalidOrClosed(connection);
				if ((idleTime > this.idleTimeout) && !removeIfInvalidOrClosed(connection)) {
					this.doRemoveConnection(connection);
				}
			}
			int fillSize = minPoolSize - this.idleConnections.size();
			if (fillSize > 0) {
				fillIdle(fillSize);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This is an internal process method, that will remove the PooledConnection
	 * from the idle and the active list and decrease the amount of connections,
	 * also, refill if necessary.
	 */
	void doRemoveConnection(DefaultPooledConnection connection) {
		lock.lock();
		try {
			if (activeConnections.remove(connection) || idleConnections.remove(connection)) {
				currentPoolSize--;
				if (currentPoolSize < minPoolSize)
					addOneToIdle();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method will create new connections and add them to the idle list if
	 * the currentPoolSize is < to finalSize
	 * 
	 * @param finalSize
	 *            Max number of connections to be added
	 */
	private void fillIdle(Integer finalSize) {
		if (logger.isDebugEnabled() && currentPoolSize < finalSize) {
			logger.debug("Filling idle connections");
		}
		lock.lock();
		try {
			while (currentPoolSize < finalSize) {
				addOneToIdle();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method will add one connection to the idle queue and increment the
	 * pool size
	 */
	private void addOneToIdle() {
		lock.lock();
		try {
			this.idleConnections.add(getDatasourceConnection());
			currentPoolSize++;
		} finally {
			lock.unlock();
		}
	}

	private DefaultPooledConnection getDatasourceConnection() {
		try {
			return dataSource.getPooledConnection();
		} catch (SQLException e) {
			throw new ConnectionPoolException("Error getting a connection from datasource", e);
		}

	}

	/**
	 * Remove the connection if it was invalid or closed, and returns true if
	 * the connection was removed and false otherwise
	 */
	private boolean removeIfInvalidOrClosed(DefaultPooledConnection connection) {
		try {
			if (!this.isValid(connection) || connection.isClosed()) {
				this.doRemoveConnection(connection);
				return true;
			}
		} catch (SQLException e) {
			logger.warn("Exception during validation of a connection, the connection will be removed from the pool", e);
		}
		return false;
	}

	private boolean isValid(DefaultPooledConnection connection) throws SQLException {
		return connection.isValid(this.validTimeout);
	}

	private DefaultPooledConnection pollIdleConnection() {
		return this.idleConnections.poll();
	}

	/**
	 * This method will move a connection from the active list to the idle list.
	 * It will not check if the connection is in the active list.
	 * 
	 * @param pooledConnection
	 *            the connection to be moved.
	 */
	private void moveToIdle(DefaultPooledConnection pooledConnection) {
		this.activeConnections.remove(pooledConnection);
		this.idleConnections.add(pooledConnection);
		pooledConnection.touch();
	}

}