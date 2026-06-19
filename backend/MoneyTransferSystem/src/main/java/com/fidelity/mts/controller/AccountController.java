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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelity.mts.application.dto.AccountResponse;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/accounts")
public class AccountController {
	
	@Autowired
	AccountService service;

	@Autowired
	RewardService rewardService;
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
