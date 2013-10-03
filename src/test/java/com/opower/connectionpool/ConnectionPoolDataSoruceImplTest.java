package com.opower.connectionpool;

import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;

import java.util.List;

import javax.sql.ConnectionEventListener;

import org.junit.Test;

import com.opower.connectionpool.connection.DefaultPooledConnection;
import com.opower.connectionpool.utils.ReflectionUtils;

public class ConnectionPoolDataSoruceImplTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetPooledConnection() throws Exception {
		ConnectionEventListener listener = createMock(ConnectionEventListener.class);
		ConnectionPoolDataSourceImpl datasource = new ConnectionPoolDataSourceImpl("la", "la", "le", listener);
		DefaultPooledConnection conn = datasource.getPooledConnection();
		List list = (List) ReflectionUtils.getPrivateProperty(conn, "connectionListeners");
		assertTrue(list.contains(listener));
	}
}
