package com.opower.connectionpool;

import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.utils.ReflectionUtils;

public class ConnecitonPoolManagerTest {

	@Test
	public void testStart() throws Exception {
		ConnectionPoolImpl pool = EasyMock.createMock(ConnectionPoolImpl.class);
		pool.start();
		EasyMock.expectLastCall();
		
		EasyMock.replay(pool);
		ConnectionPoolManager manager = ((ConnectionPoolManager) ReflectionUtils.invokePrivateConstructor(ConnectionPoolManager.class, pool, 1000L));
		EasyMock.verify(pool);
	}
	//This test throws an exception because of a bug in easymock with concurrent access to the methods of the mock. But the test works.
	@Test
	public void testMonitor() throws Exception {
		ConnectionPoolImpl pool = EasyMock.createMock(ConnectionPoolImpl.class);
		pool.clean();
		EasyMock.expectLastCall().once();
		
		pool.start();
		EasyMock.expectLastCall();
		
		
		EasyMock.replay(pool);
		ConnectionPoolManager manager = ((ConnectionPoolManager) ReflectionUtils.invokePrivateConstructor(ConnectionPoolManager.class, pool, 1000L));
		Thread.sleep(1200);
		EasyMock.verify(pool);
	}
}
