package com.fidelity.mts.service;

import java.math.BigDecimal;
import java.util.List;

import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.domain.model.TransactionLog;

public interface AccountService {
    Account getAccount(long id);
    BigDecimal getBalance(long id);
    List<TransactionLog> getTransactions(long id);
}
