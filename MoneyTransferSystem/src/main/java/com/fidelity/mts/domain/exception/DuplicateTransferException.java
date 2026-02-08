package com.fidelity.mts.domain.exception;

import java.util.UUID;

@SuppressWarnings("serial")
public class DuplicateTransferException extends RuntimeException {


    public DuplicateTransferException(UUID idempotencyKey) {
        super("Duplicate transfer detected with ID: " + idempotencyKey);
    }

}
