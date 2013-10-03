package com.opower.connectionpool;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.apache.log4j.Logger;

import com.opower.connectionpool.connection.DefaultPooledConnection;

public class PooledConnectionEventListener implements ConnectionEventListener {
	private static final Logger log = Logger.getLogger(PooledConnectionEventListener.class);

	private ConnectionPoolImpl pool;

	public PooledConnectionEventListener(ConnectionPoolImpl pool) {
		super();
		this.pool = pool;
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		if (event.getSQLException() != null) {
			log.error("Exception found whyle closing the connection", event.getSQLException());
		} else {
			DefaultPooledConnection connection = (DefaultPooledConnection) event.getSource();
			pool.doRemoveConnection(connection);
		}
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		log.error("Error happends on while clossing connection", event.getSQLException());
	}

}
