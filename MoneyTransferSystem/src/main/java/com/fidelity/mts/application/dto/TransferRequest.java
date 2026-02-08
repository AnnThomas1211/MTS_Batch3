package com.fidelity.mts.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(long fromAccountId, long toAccountId,
		BigDecimal amount, UUID idempotencyKey) {

}