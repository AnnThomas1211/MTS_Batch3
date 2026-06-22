package com.fidelity.mts.service;

import java.math.BigDecimal;
import java.util.List;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.domain.model.TransactionLog;

public interface AccountService {
    AccountResponse getAccount(long id);
    BigDecimal getBalance(long id);
    List<TransactionLog> getTransactions(long id);
    AccountResponse login(long accountId, String rawPassword);
    AccountResponse register(String holderName, String rawPassword);
}
