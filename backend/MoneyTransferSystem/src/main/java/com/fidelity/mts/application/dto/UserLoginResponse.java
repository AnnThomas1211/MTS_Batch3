package com.fidelity.mts.application.dto;

public class UserLoginResponse {

    private long accountId;
    private String holderName;
    private String message;

    public UserLoginResponse() {}

    public UserLoginResponse(long accountId, String holderName, String message) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.message = message;
    }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
