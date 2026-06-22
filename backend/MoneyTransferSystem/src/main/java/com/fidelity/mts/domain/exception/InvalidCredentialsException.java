package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Incorrect password. Please check your password and try again.");
	}

}
