package com.opower.connectionpool.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.opower.connectionpool.exceptions.PooledConnectionException;

/**
 * This is the proxy the ConnectionPool will give when someone ask for a
 * Connection. This is used to check if the connection hasn't been returned to
 * the pool and in this the pool can get back it's connection if a certaing
 * inactive time passed.
 * 
 * @author German Otero
 * 
 */
public class PooledConnectionInvocationHandler implements InvocationHandler {
	private DefaultPooledConnection pooledConnection;

	public PooledConnectionInvocationHandler(DefaultPooledConnection pooledConnection) {
		super();
		this.pooledConnection = pooledConnection;
		pooledConnection.setInvocationHandler(this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (pooledConnection == null) {
			throw new PooledConnectionException(
					"The connection you are trying to access has been grabed by the pool because of a timeout. You need to get a new one");
		}
		if (Object.class == method.getDeclaringClass()) {
			String name = method.getName();
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		pooledConnection.touch();
		return method.invoke(pooledConnection.getConnection(), args);
	}

	/**
	 * This method will clear the inner pooled connection, and the proxy that
	 * uses this handler will no longer access to that connection
	 */
	public void clearConnection() {
		this.pooledConnection = null;
	}

	public DefaultPooledConnection getPooledConnection() {
		return pooledConnection;
	}

}
