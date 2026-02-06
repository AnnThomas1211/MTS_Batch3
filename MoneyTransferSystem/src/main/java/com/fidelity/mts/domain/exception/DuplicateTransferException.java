package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class DuplicateTransferException extends RuntimeException {

    private final String transferId;

    public DuplicateTransferException(String transferId) {
        super("Duplicate transfer detected with ID: " + transferId);
        this.transferId = transferId;
    }

    public String getTransferId() {
        return transferId;
    }
}
