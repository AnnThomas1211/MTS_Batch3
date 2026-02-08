package com.fidelity.mts.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

public record AccountResponse(
		long id,
		String holderName,
		BigDecimal balance,
		AccountStatus status,
		LocalDateTime lastUpdated
		) {
	
}

