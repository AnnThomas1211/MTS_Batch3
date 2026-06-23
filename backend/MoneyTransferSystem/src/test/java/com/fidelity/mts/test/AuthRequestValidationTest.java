package com.fidelity.mts.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.mts.application.dto.LoginRequest;
import com.fidelity.mts.application.dto.RegisterRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AuthRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setup() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void login_valid_hasNoViolations() {
		Set<ConstraintViolation<LoginRequest>> v = validator.validate(new LoginRequest(1L, "1234"));
		assertTrue(v.isEmpty());
	}

	@Test
	void login_nullAccountId_isInvalid() {
		Set<ConstraintViolation<LoginRequest>> v = validator.validate(new LoginRequest(null, "1234"));
		assertFalse(v.isEmpty());
	}

	@Test
	void login_nonPositiveAccountId_isInvalid() {
		Set<ConstraintViolation<LoginRequest>> v = validator.validate(new LoginRequest(0L, "1234"));
		assertFalse(v.isEmpty());
	}

	@Test
	void login_blankPassword_isInvalid() {
		Set<ConstraintViolation<LoginRequest>> v = validator.validate(new LoginRequest(1L, "  "));
		assertFalse(v.isEmpty());
	}

	@Test
	void register_valid_hasNoViolations() {
		Set<ConstraintViolation<RegisterRequest>> v = validator.validate(new RegisterRequest("Alice", "1234"));
		assertTrue(v.isEmpty());
	}

	@Test
	void register_blankName_isInvalid() {
		Set<ConstraintViolation<RegisterRequest>> v = validator.validate(new RegisterRequest("", "1234"));
		assertFalse(v.isEmpty());
	}

	@Test
	void register_shortPassword_isInvalid() {
		Set<ConstraintViolation<RegisterRequest>> v = validator.validate(new RegisterRequest("Alice", "12"));
		assertFalse(v.isEmpty());
	}
}
