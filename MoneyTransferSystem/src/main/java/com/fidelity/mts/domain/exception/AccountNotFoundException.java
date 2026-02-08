package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class AccountNotFoundException extends RuntimeException {

	 private final String accountId;

	    public AccountNotFoundException(String accountId) {
	        super("Account not found!");
	        this.accountId = accountId;
	    }

	    public String getAccountId() {
	        return accountId;
	    }
	
}
