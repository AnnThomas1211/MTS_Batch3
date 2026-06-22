package com.fidelity.mts.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@NotNull(message = "Account ID is required.")
		@Positive(message = "Account ID must be a positive number.")
		Long accountId,

		@NotBlank(message = "Password is required.")
		String password
		) {
}
