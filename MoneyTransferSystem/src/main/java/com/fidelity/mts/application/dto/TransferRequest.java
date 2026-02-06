package com.fidelity.mts.application.dto;

import java.util.UUID;

public record TransferRequest(Integer fromAccountId,Integer toAccountId,
		Long amount, UUID idempotencyKey) {

}