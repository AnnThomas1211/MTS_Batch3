package com.fidelity.mts.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(Integer fromAccountId,Integer toAccountId,
		BigDecimal amount, UUID idempotencyKey) {

}