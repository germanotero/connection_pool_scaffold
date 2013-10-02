package com.opower.connectionpool.exceptions;
/**
 * Exception generated on loading of the configuration file.
 *
 * @author German Otero
 */
public class ConfigurationLoadException extends RuntimeException {

	
	
	public ConfigurationLoadException(String arg0) {
		super(arg0);
	}


	public ConfigurationLoadException(String message, Throwable e) {
		super(message, e);
	}
	
	
	public static void validateNotNull(String message, Object object){
		if (object == null) {
			throw new ConfigurationLoadException(message);
		}
	}
}
