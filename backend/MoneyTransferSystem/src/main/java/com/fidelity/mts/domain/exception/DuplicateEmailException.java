package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("An account with this email already exists.");
    }
}
