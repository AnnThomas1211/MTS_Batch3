package com.fidelity.mts.test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.mts.application.dto.TransferRequest;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransferRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(500), UUID.randomUUID());
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no violations for a valid request");
    }

    @Test
    void testInvalidAmount() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(-100), UUID.randomUUID());
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Expected violations for negative amount");
    }

    @Test
    void testNullFields() {
        TransferRequest request = new TransferRequest(null, null, null, null);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Expected violations for null fields");
    }
}

