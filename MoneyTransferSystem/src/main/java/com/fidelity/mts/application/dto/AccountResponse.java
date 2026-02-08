package com.fidelity.mts.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.enums.Enums.TransactionStatus;
import com.fidelity.mts.domain.model.Account;

public record AccountResponse(
		long id,
		String holderName,
		BigDecimal balance,
		AccountStatus status,
		LocalDateTime lastUpdated
		) {

    public static AccountResponse fromAccount(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getHolderName(),
            account.getBalance(),
            account.getStatus(),
            account.getLastUpdated()
        );
    }
}

