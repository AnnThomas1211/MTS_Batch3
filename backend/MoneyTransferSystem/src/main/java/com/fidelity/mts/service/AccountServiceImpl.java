package com.fidelity.mts.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.repo.AccountRepository;
import com.fidelity.mts.repo.TransactionLogRepository;


@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	AccountRepository accountRepository;
	
	@Autowired
	TransactionLogRepository transactionLogRepository;
	
	
	@Override
    public AccountResponse getAccount(long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
        
        return AccountResponse.fromAccount(account);
    }

	@Override
	 public BigDecimal getBalance(long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
        
        return account.getBalance();
    }


	@Override
	public List<TransactionLog> getTransactions(long id) {
		List<TransactionLog> log = transactionLogRepository.findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);
		return log;
	}
	
}
