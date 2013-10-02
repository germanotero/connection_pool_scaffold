package com.opower.connectionpool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.opower.connectionpool.exceptions.ConfigurationLoadException;
import com.opower.connectionpool.impl.ConnectionPoolImpl;
import com.opower.connectionpool.impl.ConnectionPoolManager;
/**
 * Connection pool factory, this factory will create a poll.
 * 
 * @author German Otero
 * 
 * @see ConnectionPool

 *
 */
public enum ConnectionPoolFactory {
	INSTANCE;
	
	// instance of the pool
	private ConnectionPoolImpl _pool;
	
	ConnectionPoolFactory() {
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream("database.properties");
		
		ConfigurationLoadException.validateNotNull("Configuration file not found database.properties at base path", stream);
		
		try {
			prop.load(stream);
		} catch (IOException e) {
			throw new ConfigurationLoadException("Error loading database.properties", e);
		}
		//TODO do the checking to verify integer in the parse, so we don't get an unhandled exception
		_pool = new ConnectionPoolImpl( 
							prop.getProperty(ConfigurationProperties.url.name()),
							prop.getProperty(ConfigurationProperties.user.name()),
							prop.getProperty(ConfigurationProperties.password.name()),
							parseInteger(prop,ConfigurationProperties.initialPoolSize.name()),
							parseInteger(prop,ConfigurationProperties.minPoolSize.name()),
							parseInteger(prop,ConfigurationProperties.maxPoolSize.name()),
							parseLong(prop, ConfigurationProperties.idleTimeout.name()),
							parseLong(prop, ConfigurationProperties.abandonedTimeout.name()));
		
		new ConnectionPoolManager(_pool, Long.parseLong(prop.getProperty(ConfigurationProperties.evictTime.name())));
		 
	}
	/**
	 * Returns a single instance of the pool
	 * @return ConnectionPool
	 */
	public ConnectionPool get() {
		return _pool;
	}
	
	public Long parseLong(Properties prop, String property) {
		String value = prop.getProperty(property);
		if (NumberUtils.isNumber(StringUtils.trim(value))) {
			return Long.parseLong(value.trim());
		}
		throw new ConfigurationLoadException(property + " Must be a long");
	}
	
	public Integer parseInteger(Properties prop, String property) {
		String value = prop.getProperty(property);
		if (NumberUtils.isNumber(StringUtils.trim(value))) {
			return Integer.parseInt(value.trim());
		}
		throw new ConfigurationLoadException(property + " Must be an integer");
	}

}
