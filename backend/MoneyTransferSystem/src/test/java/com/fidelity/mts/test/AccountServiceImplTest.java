package com.fidelity.mts.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.domain.exception.InvalidCredentialsException;
import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.repo.AccountRepository;
import com.fidelity.mts.repo.TransactionLogRepository;
import com.fidelity.mts.service.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionLogRepository transactionLogRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AccountServiceImpl accountService;

	private Account activeAccountWithHash() {
		Account account = new Account();
		account.setId(5L);
		account.setHolderName("Alice");
		account.setBalance(BigDecimal.valueOf(1000));
		account.setStatus(AccountStatus.ACTIVE);
		account.setLastUpdated(LocalDateTime.now());
		account.setPasswordHash("BCRYPT_HASH");
		return account;
	}

	// ---------- login ----------

	@Test
	void login_withCorrectPassword_returnsAccount() {
		Account account = activeAccountWithHash();
		when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
		when(passwordEncoder.matches("1234", "BCRYPT_HASH")).thenReturn(true);

		AccountResponse response = accountService.login(5L, "1234");

		assertEquals(5L, response.id());
		assertEquals("Alice", response.holderName());
		assertEquals(AccountStatus.ACTIVE, response.status());
	}

	@Test
	void login_whenAccountMissing_throwsNotFound() {
		when(accountRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(AccountNotFoundException.class, () -> accountService.login(99L, "1234"));
		verify(passwordEncoder, never()).matches(anyString(), anyString());
	}

	@Test
	void login_withWrongPassword_throwsInvalidCredentials() {
		Account account = activeAccountWithHash();
		when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
		when(passwordEncoder.matches("wrong", "BCRYPT_HASH")).thenReturn(false);

		assertThrows(InvalidCredentialsException.class, () -> accountService.login(5L, "wrong"));
	}

	@Test
	void login_whenNoHashStored_throwsInvalidCredentials() {
		Account account = activeAccountWithHash();
		account.setPasswordHash(null);
		when(accountRepository.findById(5L)).thenReturn(Optional.of(account));

		assertThrows(InvalidCredentialsException.class, () -> accountService.login(5L, "1234"));
		// Must not even attempt to match against a null hash.
		verify(passwordEncoder, never()).matches(anyString(), any());
	}

	// ---------- register ----------

	@Test
	void register_persistsHashedPasswordAndDefaults() {
		when(passwordEncoder.encode("secret")).thenReturn("HASHED_SECRET");
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
			Account toSave = invocation.getArgument(0);
			toSave.setId(42L); // simulate DB-assigned id
			return toSave;
		});

		AccountResponse response = accountService.register("  Bob  ", "secret");

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		verify(accountRepository).save(captor.capture());
		Account saved = captor.getValue();

		assertEquals("Bob", saved.getHolderName(), "name should be trimmed");
		assertEquals("HASHED_SECRET", saved.getPasswordHash(), "raw password must never be stored");
		assertEquals(0, BigDecimal.valueOf(1000).compareTo(saved.getBalance()),
				"new accounts start with the $1000 welcome balance");
		assertEquals(AccountStatus.ACTIVE, saved.getStatus());

		assertEquals(42L, response.id());
		assertEquals("Bob", response.holderName());
	}

	@Test
	void register_neverStoresRawPassword() {
		when(passwordEncoder.encode(eq("plain-text-pw"))).thenReturn("ENCODED");
		when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

		accountService.register("Carol", "plain-text-pw");

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		verify(accountRepository).save(captor.capture());
		assertNotEquals("plain-text-pw", captor.getValue().getPasswordHash());
	}
}
