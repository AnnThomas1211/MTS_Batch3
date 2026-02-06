package com.fidelity.mts.application.dto;

public class AccountResponse {
	private int id;
	private String holderName;
	private long balance;
	private String status;
	private int version;
	private String lastUpdated;

	public AccountResponse() {
	}

	public int getId() {
	    return id;
	}

	public void setId(int id) {
	    this.id = id;
	}

	public String getHolderName() {
		return this.holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}
	public long getBalance() {
	    return balance;
	}

	public void setBalance(long balance) {
	    this.balance = balance;
	}

	public String getStatus() {
	    return status;
	}

	public void setStatus(String status) {
	    this.status = status;
	}

	public int getVersion() {
	    return version;
	}

	public void setVersion(int version) {
	    this.version = version;
	}

	public String getLastUpdated() {
	    return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
	    this.lastUpdated = lastUpdated;
	}


}
