package com.fidelity.mts.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.domain.exception.InvalidCredentialsException;
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

	@Autowired
	PasswordEncoder passwordEncoder;

	
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

	@Override
	public AccountResponse login(long accountId, String rawPassword) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new AccountNotFoundException(accountId));

		// An account with no hash set can never authenticate; treat as bad credentials.
		if (!StringUtils.hasText(account.getPasswordHash())
				|| !passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return AccountResponse.fromAccount(account);
	}

	// New accounts are credited with this welcome balance on registration.
	private static final BigDecimal STARTING_BALANCE = BigDecimal.valueOf(1000);

	@Override
	public AccountResponse register(String holderName, String rawPassword) {
		Account account = new Account();
		account.setHolderName(holderName.trim());
		account.setBalance(STARTING_BALANCE);
		account.setStatus(AccountStatus.ACTIVE);
		account.setLastUpdated(LocalDateTime.now());
		account.setPasswordHash(passwordEncoder.encode(rawPassword));

		Account saved = accountRepository.save(account);
		return AccountResponse.fromAccount(saved);
	}

}
