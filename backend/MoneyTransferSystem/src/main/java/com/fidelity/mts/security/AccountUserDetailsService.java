package com.fidelity.mts.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.repo.AccountRepository;

/**
 * Authenticates HTTP Basic requests per account instead of against a single
 * global user. The "username" is the account id and the stored credential is
 * the account's BCrypt password hash. Spring's DaoAuthenticationProvider then
 * matches the raw password using the configured PasswordEncoder.
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

	@Autowired
	private AccountRepository accountRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		long accountId;
		try {
			accountId = Long.parseLong(username.trim());
		} catch (NumberFormatException ex) {
			throw new UsernameNotFoundException("Invalid account id: " + username);
		}

		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new UsernameNotFoundException("No account found with id: " + accountId));

		if (!StringUtils.hasText(account.getPasswordHash())) {
			throw new UsernameNotFoundException("No password set for account id: " + accountId);
		}

		return User.withUsername(String.valueOf(account.getId()))
			.password(account.getPasswordHash())
			.authorities("USER")
			.build();
	}
}
