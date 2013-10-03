package com.opower.connectionpool;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;

import com.opower.connectionpool.connection.DefaultPooledConnection;

public class ConnectionPoolDataSourceImpl implements ConnectionPoolDataSource {
	private String url;
	private String user;
	private String password;
	private PrintWriter log;
	private ConnectionEventListener listener;

	/**
	 * Create a new ConnectionPoolDataSource. Use constructor injection since
	 * all arguments are required.
	 * 
	 * @param driverClassName
	 * @param dbUrl
	 * @param user
	 * @param password
	 * @param listener
	 */
	public ConnectionPoolDataSourceImpl(String url, String user, String password, ConnectionEventListener listener) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.listener = listener;
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	@Override
	public void setLoginTimeout(int loginTimeout) throws SQLException {
		DriverManager.setLoginTimeout(loginTimeout);
	}

	@Override
	public DefaultPooledConnection getPooledConnection() throws SQLException {
		return getPooledConnection(this.user, this.password);
	}

	@Override
	public DefaultPooledConnection getPooledConnection(String dbUser, String dbPassword) throws SQLException {
		DefaultPooledConnection connection = new DefaultPooledConnection(this.url, dbUser, dbPassword);
		connection.addConnectionEventListener(this.listener);
		return connection;

	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return log;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.log = out;
	}

}
