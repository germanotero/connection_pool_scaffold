package com.opower.connectionpool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.opower.connectionpool.driver.TestDriver;
import com.opower.connectionpool.exceptions.ConnectionPoolException;
import com.opower.connectionpool.utils.ReflectionUtils;

public class ConnectionPoolImplTest {

	@BeforeClass
	public static void before() throws SQLException {
		TestDriver.getInstance();
	}

	@Test
	public void testInitialSizeLess1() {
		try {
			new ConnectionPoolImpl("la", "user", "pass", 0, 1, 10, 1000L, 10000L);
			fail("You can't create a pool with less than 1 in the initial size");
		} catch (IllegalArgumentException e) {
			assertEquals("Initial pool size must be > 1", e.getMessage());
		}
		try {
			new ConnectionPoolImpl("la", "user", "pass", -10, 1, 10, 1000L, 10000L);
			fail("You can't create a pool with less than 1 in the initial size");
		} catch (IllegalArgumentException e) {
			assertEquals("Initial pool size must be > 1", e.getMessage());
		}
	}

	@Test
	public void testInitialMoreThanMax() {
		try {
			new ConnectionPoolImpl("la", "user", "pass", 11, 1, 10, 1000L, 10000L);
			fail("You can't create a pool with initial size > than max");
		} catch (IllegalArgumentException e) {
			assertEquals("Initial pool size, can't be higher than Max pool size", e.getMessage());
		}
	}

	@Test
	public void testInitialLessThanMin() {
		try {
			new ConnectionPoolImpl("la", "user", "pass", 1, 5, 10, 1000L, 10000L);
			fail("You can't create a pool with initial size < than min");
		} catch (IllegalArgumentException e) {
			assertEquals("Initial pool size, can't be lower than Min pool size", e.getMessage());
		}
	}

	@Test
	public void testStart() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		int size = (Integer) ReflectionUtils.getPrivateProperty(pool, "currentPoolSize");
		assertEquals(5, size);
	}

	@Test
	public void testGetConnectionNotStarted() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		try {
			pool.getConnection();
			fail("Pool shouldn't give a connection if it hasn't been started");
		} catch (ConnectionPoolException e) {
			assertEquals(
					"Connection pool is empty. Usually this means the pool hasn't been started or all connections in the pool has been taken",
					e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetConnectionStarted() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetAll5Connections() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		for (int i = 0; i < 5; i++) {
			Connection conn = pool.getConnection();
			assertNotNull(conn);
		}
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(5, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(0, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetOneMoreConnection() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		for (int i = 0; i < 5; i++) {
			Connection conn = pool.getConnection();
			assertNotNull(conn);
		}
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(6, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(0, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetAllPossibleConnections() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		for (int i = 0; i < 10; i++) {
			Connection conn = pool.getConnection();
			assertNotNull(conn);
		}
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(10, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(0, idle.size());
		try {
			pool.getConnection();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testReturnOneConnection() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());

		pool.releaseConnection(conn);
		active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(0, active.size());
		idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(5, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testReturnOneClosedConnection() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());

		conn.close();
		pool.releaseConnection(conn);

		active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(0, active.size());
		idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(5, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testReturnOneUnknownConnection() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());

		pool.releaseConnection(new TestDriver.TestConnection());

		active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testReturnOneConnectionAndGrabBack() throws Exception {
		ConnectionPoolImpl pool = new ConnectionPoolImpl("la", "user", "pass", 5, 5, 10, 1000L, 10000L);
		pool.start();
		Connection conn = pool.getConnection();
		assertNotNull(conn);
		Collection active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		Collection idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());

		pool.releaseConnection(conn);
		active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(0, active.size());
		idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(5, idle.size());

		conn = pool.getConnection();
		assertNotNull(conn);
		active = (Collection) ReflectionUtils.getPrivateProperty(pool, "activeConnections");
		assertEquals(1, active.size());
		idle = (Collection) ReflectionUtils.getPrivateProperty(pool, "idleConnections");
		assertEquals(4, idle.size());
	}
}
