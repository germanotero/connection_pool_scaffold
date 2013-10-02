package com.opower.connectionpool.exceptions;

public class PooledConnectionException extends RuntimeException {

	public PooledConnectionException() {
		super();
	}

	public PooledConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PooledConnectionException(String message) {
		super(message);
	}

	public PooledConnectionException(Throwable cause) {
		super(cause);
	}

}
