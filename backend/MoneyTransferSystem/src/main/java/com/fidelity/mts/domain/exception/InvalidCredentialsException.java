package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}
