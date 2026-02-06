package com.fidelity.mts.application.dto;

import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

public record TransferResponse(String transferId, long fromAccountNewBalance, 
		long toAccountNewBalance, int fromAccountNewVersion, int toAccountNewVersion,
		TransactionStatus status) {

}
