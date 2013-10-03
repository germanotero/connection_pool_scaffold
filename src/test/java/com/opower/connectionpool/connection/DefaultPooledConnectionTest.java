package com.opower.connectionpool.connection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.opower.connectionpool.driver.TestDriver;

public class DefaultPooledConnectionTest {
	
	@BeforeClass
	public static void before() {
		TestDriver.getInstance();
	}
	
	@Test
	public void testConstructor() {
		TestDriver.getInstance().connections.clear();
		DefaultPooledConnection conn = new DefaultPooledConnection("la", "user","pass");
		assertEquals(1, TestDriver.INSTANCE.connections.size());
		assertTrue((System.currentTimeMillis() - conn.getLastTouch()) < 1000);
	}

	@Test
	public void testClose() throws SQLException {
		TestDriver.getInstance().connections.clear();
		DefaultPooledConnection conn = new DefaultPooledConnection("la", "user","pass");
		conn.close();
		//It must have two.
		assertTrue(TestDriver.INSTANCE.connections.get(0).isClosed());
	}
}
