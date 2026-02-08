package com.fidelity.mts.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(@NotNull Long fromAccountId,@NotNull Long toAccountId,
		@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull UUID idempotencyKey) {



}