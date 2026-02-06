package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class AccountNotActiveException extends RuntimeException {
	
	 private final String accountId;

	    public AccountNotActiveException(String accountId) {
	        super("Account not active with ID: " + accountId);
	        this.accountId = accountId;
	    }

	    public String getAccountId() {
	        return accountId;
	    }
}
