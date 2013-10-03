package com.opower.connectionpool;

import javax.sql.ConnectionEvent;

import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.connection.DefaultPooledConnection;

public class PooledConnectionEventListenerTest {
	
	@Test
	public void testClose() {
		
		DefaultPooledConnection conn = EasyMock.createMock(DefaultPooledConnection.class);
		
		ConnectionPoolImpl pool = EasyMock.createMock(ConnectionPoolImpl.class);
		pool.doRemoveConnection(conn);
		EasyMock.expectLastCall();
		
		ConnectionEvent event = EasyMock.createMock(ConnectionEvent.class);
		EasyMock.expect(event.getSQLException()).andReturn(null);
		EasyMock.expect(event.getSource()).andReturn(conn);
		
		EasyMock.replay(pool, conn, event);
		
		PooledConnectionEventListener listener = new PooledConnectionEventListener(pool);
		listener.connectionClosed(event);
		
		EasyMock.verify(pool, conn, event);
	}

}
