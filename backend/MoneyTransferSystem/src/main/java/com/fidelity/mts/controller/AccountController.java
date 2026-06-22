package com.fidelity.mts.controller;

import java.math.BigDecimal;
import java.util.List;

import com.fidelity.mts.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.application.dto.LoginRequest;
import com.fidelity.mts.application.dto.RegisterRequest;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.service.AccountService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/accounts")
public class AccountController {
	
	@Autowired
	AccountService service;

	@Autowired
	RewardService rewardService;

	@PostMapping("/login")
	public ResponseEntity<AccountResponse> login(@Valid @RequestBody LoginRequest request) {
		AccountResponse account = service.login(request.accountId(), request.password());
		return new ResponseEntity<>(account, HttpStatus.OK);
	}

	@PostMapping("/register")
	public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
		AccountResponse account = service.register(request.holderName(), request.password());
		return new ResponseEntity<>(account, HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AccountResponse> getAccountById(@PathVariable long id) {
		return new ResponseEntity<>(service.getAccount(id), HttpStatus.OK);
	}
	
	@GetMapping("/{id}/balance")
	public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable long id) {
		return new ResponseEntity<>(service.getBalance(id), HttpStatus.OK);
	}
	
	@GetMapping("/{id}/transactions")
	public ResponseEntity<List<TransactionLog>> getTransactions(@PathVariable long id) {
		return new ResponseEntity<>(service.getTransactions(id), HttpStatus.OK);
	}


	@GetMapping("/rewards/{accountId}")
	public ResponseEntity<?> viewRewards(@PathVariable long accountId) {
		return new ResponseEntity<>(rewardService.getRewardHistory(accountId), HttpStatus.OK);
	}
	
	
}
