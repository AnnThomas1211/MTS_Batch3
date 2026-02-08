package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class AccountNotFoundException extends RuntimeException {


	    public AccountNotFoundException(long accountId) {
	        super("Account not found with ID: " + accountId);

	    }

}
