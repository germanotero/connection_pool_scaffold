package com.opower.connectionpool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.createMockBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.opower.connectionpool.exceptions.PooledConnectionException;
import com.opower.connectionpool.impl.ConnectionPoolDataSourceImpl;
import com.opower.connectionpool.impl.ConnectionPoolException;
import com.opower.connectionpool.impl.ConnectionPoolImpl;

public class ConnectionPoolTest {

	List<Connection> connections = Lists.newArrayList();

	@Before
	public void before() throws SecurityException, NoSuchMethodException, SQLException {
		ConnectionPoolDataSourceImpl connectionPoolDatasource = createMockBuilder(ConnectionPoolDataSourceImpl.class)
				.addMockedMethod(ConnectionPoolDataSourceImpl.class.getMethod("getPooledConnection", new Class[0]))
				.createMock();
		EasyMock.expect(connectionPoolDatasource.getPooledConnection()).andReturn(null);
	}

	@After
	public void after() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		for (Connection conn : this.connections) {
			pool.releaseConnection(conn);
		}
		connections.clear();
	}

	@Test
	public void testInstance() {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		assertEquals(ConnectionPoolImpl.class, pool.getClass());
	}

	@Test
	public void test10Connections() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);

		for (int i = 0; i < 10; i++) {
			Connection connection = pool.getConnection();
			assertNotNull(connection);
			connections.add(connection);
		}

	}

	@Test
	public void testActiveConnectionsLimit() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		for (int i = 0; i < 20; i++) {
			Connection connection = pool.getConnection();
			assertNotNull(connection);
			this.connections.add(connection);
		}
		try {
			this.connections.add(pool.getConnection());
			fail("Pool shoul have been full");
		} catch (ConnectionPoolException ex) {

		}
	}

	@Test
	public void testGetConnection() throws SQLException, SecurityException, NoSuchMethodException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		this.connections.add(pool.getConnection());
	}

	@Test
	public void testLimitAfterRelease() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		for (int i = 0; i < 20; i++) {
			Connection connection = pool.getConnection();
			assertNotNull(connection);
			this.connections.add(connection);
		}
		try {
			this.connections.add(pool.getConnection());
			fail("Pool shoul have been full");
		} catch (ConnectionPoolException ex) {
		}

		pool.releaseConnection(this.connections.remove(0));
		this.connections.add(pool.getConnection());
		try {
			this.connections.add(pool.getConnection());
			fail("Pool shoul have been full");
		} catch (ConnectionPoolException ex) {
		}
	}

	@Test
	public void testGrab() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		Connection conn = pool.getConnection();
		conn.toString();
		try {
			Thread.sleep(35000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			conn.clearWarnings();
			fail("There should have throw an exception");
		} catch (PooledConnectionException e) {
			System.out.println(e.getMessage());
		}

	}
}
