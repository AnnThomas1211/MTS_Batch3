package com.fidelity.mts.domain.exception;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class InsufficientBalanceException extends RuntimeException {

    private final String accountId;
    private final BigDecimal attemptedAmount;
    private final BigDecimal currentBalance;

    public InsufficientBalanceException(String accountId,
                                        BigDecimal attemptedAmount,
                                        BigDecimal currentBalance) {
        super("Insufficient balance in account " + accountId +
              ". Attempted: " + attemptedAmount +
              ", Available: " + currentBalance);
        this.accountId = accountId;
        this.attemptedAmount = attemptedAmount;
        this.currentBalance = currentBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAttemptedAmount() {
        return attemptedAmount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
}