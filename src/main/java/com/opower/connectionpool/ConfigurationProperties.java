package com.opower.connectionpool;
/**
 * This enum, contains the constants for the configuration property file.
 *
 * @author German Otero
 */
public enum ConfigurationProperties {
	driver,
	url,
	user,
	password,
	minPoolSize,
	maxPoolSize,
	connectionTimeout,
	pollTimeout, 
	initialPoolSize, 
	idleTimeout, 
	abandonedTimeout, 
	evictTime;
}
