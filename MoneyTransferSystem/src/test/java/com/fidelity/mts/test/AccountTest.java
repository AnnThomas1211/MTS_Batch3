package com.fidelity.mts.test;

import org.junit.jupiter.api.Test;

import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.exception.InsufficientBalanceException;
import com.fidelity.mts.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testDebit_Success() {
        Account account = new Account(
                1L,
                "John",
                BigDecimal.valueOf(1000),
                AccountStatus.ACTIVE,
                1, // version
                LocalDateTime.now() // lastUpdated
        );

        account.debit(BigDecimal.valueOf(500));
        assertEquals(BigDecimal.valueOf(500), account.getBalance());
    }

    @Test
    void testDebit_InsufficientBalance() {
        Account account = new Account(
                1L,
                "John",
                BigDecimal.valueOf(300),
                AccountStatus.ACTIVE,
                1,
                LocalDateTime.now()
        );

        assertThrows(InsufficientBalanceException.class,
                () -> account.debit(BigDecimal.valueOf(500)));
    }

    @Test
    void testCredit_Success() {
        Account account = new Account(
                1L,
                "John",
                BigDecimal.valueOf(1000),
                AccountStatus.ACTIVE,
                1,
                LocalDateTime.now()
        );

        account.credit(BigDecimal.valueOf(200));
        assertEquals(BigDecimal.valueOf(1200), account.getBalance());
    }
}
