package com.sngular.adriangm.myapp.exception;

public class ExternalServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ExternalServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
