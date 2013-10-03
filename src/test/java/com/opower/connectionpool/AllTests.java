package com.opower.connectionpool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.opower.connectionpool.connection.DefaultPooledConnectionTest;

@RunWith(Suite.class)
@SuiteClasses({ ConnectionPoolFactoryTest.class, ConnectionPoolTest.class, ConnectionPoolDataSoruceImplTest.class,
		DefaultPooledConnectionTest.class, ConnectionPoolImplTest.class, ConnecitonPoolManagerTest.class,
		PooledConnectionEventListenerTest.class })
public class AllTests {

}
