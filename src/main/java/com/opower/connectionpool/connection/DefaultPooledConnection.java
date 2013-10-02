package com.opower.connectionpool.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.opower.connectionpool.exceptions.DatabaseConnectionException;

/**
 * A wrapper of java.sql.Connection with event listeners.
 * 
 * @author German Otero
 * 
 * @see Connection
 * 
 */
public class DefaultPooledConnection implements PooledConnection {

	private Connection connection;
	private List<ConnectionEventListener> connectionListeners = Lists.newArrayList();
	private List<StatementEventListener> statementListeners = Lists.newArrayList();
	private PooledConnectionInvocationHandler invocationHandler;
	private Long lastTouch;
	

	/**
	 * Construct a new PooledConnectionImpl, needs url, user and password.
	 * 
	 * @param dbUrl
	 * @param user
	 * @param password
	 * 
	 * @throws IllegalArgumentException
	 *             if url or user are null or empty.
	 * 
	 * @throws DatabaseConnecitonException
	 *             if there's an error when trying to create the wraped
	 *             connection
	 */
	public DefaultPooledConnection(String url, String user, String password) {
		if (Strings.isNullOrEmpty(url) || Strings.isNullOrEmpty(user)) {
			throw new IllegalArgumentException("User or Url can't be null");
		}
		try {
			connection = DriverManager.getConnection(url, user, password);
			touch();
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Error connecting to database. url:" + url + " user: " + user, e);
		}
	}

	/**
	 * Closes the connection and release it for the pool.
	 * 
	 * @throws DatabaseConnectionException
	 *             if an exception happens during the close of the wrapped
	 *             connection.
	 */
	public void close() {
		try {
			this.connection.close();
			for (ConnectionEventListener listener : connectionListeners) {
				listener.connectionClosed(new ConnectionEvent(this));
			}
		} catch (SQLException e) {
			for (ConnectionEventListener listener : connectionListeners) {
				listener.connectionErrorOccurred(new ConnectionEvent(this, e));
			}
			throw new DatabaseConnectionException("Exception when closing PooledConnection", e);
		}
	}

	/**
	 * Returns the wrapped connection.
	 */
	public Connection getConnection() {
		touch();
		return this.connection;
	}

	/**
	 * Removes a Connection event listener.
	 */
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		connectionListeners.remove(listener);
	}

	/**
	 * Removes an Statement event listener
	 */
	public void removeStatementEventListener(StatementEventListener listener) {
		statementListeners.remove(listener);

	}

	/**
	 * Adds a connection event listener
	 */
	public void addConnectionEventListener(ConnectionEventListener listener) {
		connectionListeners.add(listener);
	}

	/**
	 * Adds an statement event listener
	 */
	public void addStatementEventListener(StatementEventListener listener) {
		statementListeners.add(listener);
	}

	public void touch() {
		lastTouch = System.currentTimeMillis();
	}
	
	public long getLastTouch() {
		return lastTouch;
	}

	public void setInvocationHandler(PooledConnectionInvocationHandler invocationHandler) {
		this.invocationHandler = invocationHandler;
	}
	
	public void releaseFromProxy() {
		this.invocationHandler.clearConnection();
		this.invocationHandler = null;
	}
	/**
	 * This method will delegate the validation of the conneciton to the inner connection
	 * @throws SQLException 
	 */
	public Boolean isValid(int timeout) throws SQLException {
		return this.connection.isValid(timeout);
	}

	public boolean isClosed() throws SQLException {
		return this.connection.isClosed();
	}
	
}