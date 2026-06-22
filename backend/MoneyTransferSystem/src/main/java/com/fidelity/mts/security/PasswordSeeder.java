package com.fidelity.mts.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.repo.AccountRepository;

/**
 * Backfills a default password for accounts that have no hash yet (e.g. rows
 * created before the password_hash column existed). This preserves the previous
 * demo behaviour where every account logged in with the same password "1234",
 * except the credential is now stored per account as a BCrypt hash.
 */
@Component
public class PasswordSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(PasswordSeeder.class);
	private static final String DEFAULT_PASSWORD = "1234";

	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;

	public PasswordSeeder(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		List<Account> accounts = accountRepository.findAll();
		int updated = 0;
		for (Account account : accounts) {
			if (!StringUtils.hasText(account.getPasswordHash())) {
				account.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
				updated++;
			}
		}
		if (updated > 0) {
			accountRepository.saveAll(accounts);
			log.info("PasswordSeeder: set default password for {} account(s) without a hash.", updated);
		}
	}
}
