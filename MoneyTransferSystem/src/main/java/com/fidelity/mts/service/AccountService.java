package com.fidelity.mts.service;

import java.math.BigDecimal;
import java.util.List;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.application.dto.TransferResponse;

public interface AccountService {
    AccountResponse getAccount(Long id);
    BigDecimal getBalance(Long id);
    List<TransferResponse> getTransactions(Long id);
}
