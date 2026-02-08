package com.fidelity.mts.application.dto;

import java.math.BigDecimal;

import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

public record TransferResponse(String transactionId,
		String status,
		String message,
		long debitedFrom,
		long creditedTo,
		BigDecimal amount) {

}
