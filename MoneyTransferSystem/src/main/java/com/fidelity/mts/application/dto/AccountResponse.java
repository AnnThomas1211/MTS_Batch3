package com.fidelity.mts.application.dto;

import java.time.Instant;

import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

public record AccountResponse(int id, String holderName, long balance,
			TransactionStatus status, int version, Instant lastUpdated) {
	
}
