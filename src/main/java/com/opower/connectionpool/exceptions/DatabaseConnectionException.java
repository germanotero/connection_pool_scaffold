package com.opower.connectionpool.exceptions;

public class DatabaseConnectionException extends RuntimeException {

	public DatabaseConnectionException() {
		super();
	}

	public DatabaseConnectionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DatabaseConnectionException(String arg0) {
		super(arg0);
	}

	public DatabaseConnectionException(Throwable arg0) {
		super(arg0);
	}

}
