package com.fidelity.mts.service;

import com.fidelity.mts.application.dto.UserLoginRequest;
import com.fidelity.mts.application.dto.UserLoginResponse;
import com.fidelity.mts.application.dto.UserRegistrationRequest;
import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.domain.model.User;
import com.fidelity.mts.domain.exception.DuplicateEmailException;
import com.fidelity.mts.domain.exception.InvalidCredentialsException;
import com.fidelity.mts.repo.AccountRepository;
import com.fidelity.mts.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String register(UserRegistrationRequest request) {
        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 1. Create a new Account — holderName = name, balance = 0, status = ACTIVE
        Account account = new Account();
        account.setHolderName(request.getName());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setLastUpdated(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        // 2. Create the User linked to that account
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getName(),
                request.getEmail(),
                hashedPassword,
                savedAccount.getId()
        );
        userRepository.save(user);

        return "Registration successful. Account #" + savedAccount.getId() + " has been created.";
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Fetch the linked account to get holderName
        Account account = accountRepository.findById(user.getAccountId())
                .orElseThrow(() -> new RuntimeException("Linked account not found."));

        return new UserLoginResponse(
                account.getId(),
                account.getHolderName(),
                "Login successful."
        );
    }
}
