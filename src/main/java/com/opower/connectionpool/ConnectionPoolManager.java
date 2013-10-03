package com.opower.connectionpool;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.opower.connectionpool.exceptions.ConnectionPoolException;
/**
 * Connection Pool Manager, this is the manager of the pool, the one that starts a monitor thread over the pool
 * @author German Otero
 *
 */
public class ConnectionPoolManager {

	private Long evictInterval;
	private volatile Boolean active;
	// I have to use my implementation so I keep the interface clean. And also,
	// this manager is special for this implementation
	private ConnectionPoolImpl pool;
	private ExecutorService es;

	ConnectionPoolManager(ConnectionPoolImpl pool, Long evictInterval) {
		this.pool = pool;
		this.evictInterval = evictInterval;
		this.es = Executors.newSingleThreadExecutor();

		try {
			this.pool.start();
		} catch (SQLException e) {
			throw new ConnectionPoolException("Exception during pool start", e);
		}

		this.active = true;
		es.execute(new MonitorRunnable());
	}
	/**
	 * Monitor for the connections pool
	 * @author German Otero
	 *
	 */
	private class MonitorRunnable implements Runnable {

		@Override
		public void run() {
			while (active) {
				try {
					Thread.sleep(evictInterval);
				} catch (InterruptedException e) {
					continue;
				}
				pool.clean();
			}
		}

	}

	public void stop() {
		this.active = false;
	}
	
}
