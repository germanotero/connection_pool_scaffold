package com.opower.connectionpool;

import static junit.framework.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opower.connectionpool.connection.DefaultPooledConnection;
import com.opower.connectionpool.driver.TestDriver;
import com.opower.connectionpool.utils.ReflectionUtils;

public class ConnectionPoolFactoryTest {
	@Before
	public void before() throws Exception {
		TestDriver.getInstance();
	}

	// I'm doing this, because it's the only way to inject a datasource into the
	// pool
	public void injectDatasource(ConnectionPoolDataSourceImpl datasource) throws Exception {
		ConnectionPool pool = ConnectionPoolFactory.INSTANCE.get();
		ReflectionUtils.setPrivateProperty(pool, "dataSource", datasource);
	}

	@Test
	public void testSingleton() {
		ConnectionPoolFactory factory = ConnectionPoolFactory.INSTANCE;
		ConnectionPoolFactory sameFactory = ConnectionPoolFactory.INSTANCE;

		assertEquals(factory, sameFactory);

	}

}
