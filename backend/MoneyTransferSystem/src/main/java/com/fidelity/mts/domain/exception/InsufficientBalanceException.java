package com.fidelity.mts.domain.exception;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(long accountId,
                                        BigDecimal attemptedAmount,
                                        BigDecimal currentBalance) {
        super("Insufficient balance in account " + accountId +
              ". Attempted: " + attemptedAmount +
              ", Available: " + currentBalance);

    }

}