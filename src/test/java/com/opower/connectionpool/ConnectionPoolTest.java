package com.opower.connectionpool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.opower.connectionpool.exceptions.ConnectionPoolException;
import com.opower.connectionpool.exceptions.PooledConnectionException;

public class ConnectionPoolTest {

	List<Connection> connections = Lists.newArrayList();

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
			Thread.sleep(35 * 1000); // 35 secs
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			conn.clearWarnings();
			fail("There should have throw an exception");
		} catch (PooledConnectionException e) {
			assertEquals(
					"The connection you are trying to access has been grabed by the pool because of a timeout. You need to get a new one",
					e.getMessage());
		}
	}

	@Test
	public void testUserClose() throws SQLException {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		assertNotNull(pool);
		Connection conn = pool.getConnection();
		conn.close();
		pool.releaseConnection(conn);

		try {
			conn = pool.getConnection();
			assertFalse(conn.isClosed());
			assertTrue(conn.isValid(100000));
			connections.add(conn);
		} catch (Exception e) {
			// ok
		}
	}
}
	