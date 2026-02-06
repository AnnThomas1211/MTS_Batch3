package com.fidelity.mts.application.dto;

public class TransferResponse {

    private String transferId;
    private long fromAccountNewBalance;
    private long toAccountNewBalance;
    private int fromAccountNewVersion;
    private int toAccountNewVersion;
    private String status;

    public TransferResponse() {
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public long getFromAccountNewBalance() {
        return fromAccountNewBalance;
    }

    public void setFromAccountNewBalance(long fromAccountNewBalance) {
        this.fromAccountNewBalance = fromAccountNewBalance;
    }

    public long getToAccountNewBalance() {
        return toAccountNewBalance;
    }

    public void setToAccountNewBalance(long toAccountNewBalance) {
        this.toAccountNewBalance = toAccountNewBalance;
    }

    public int getFromAccountNewVersion() {
        return fromAccountNewVersion;
    }

    public void setFromAccountNewVersion(int fromAccountNewVersion) {
        this.fromAccountNewVersion = fromAccountNewVersion;
    }

    public int getToAccountNewVersion() {
        return toAccountNewVersion;
    }

    public void setToAccountNewVersion(int toAccountNewVersion) {
        this.toAccountNewVersion = toAccountNewVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
