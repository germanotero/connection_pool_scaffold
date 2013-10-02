package com.opower.connectionpool;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;


public class ConnectionPoolFactoryTest {
	
	@Test
	public void testSingleton() {
		ConnectionPoolFactory factory = ConnectionPoolFactory.INSTANCE;
		ConnectionPoolFactory sameFactory = ConnectionPoolFactory.INSTANCE;
		
		assertEquals(factory, sameFactory);
				
	}

}
