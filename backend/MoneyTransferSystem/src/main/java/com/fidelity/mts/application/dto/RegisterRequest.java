package com.fidelity.mts.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

		@NotBlank(message = "Name is required.")
		@Size(max = 100, message = "Name must be at most 100 characters.")
		String holderName,

		@NotBlank(message = "Password is required.")
		@Size(min = 4, message = "Password must be at least 4 characters.")
		String password
		) {
}
