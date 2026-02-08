package com.fidelity.mts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelity.mts.application.dto.TransferRequest;
import com.fidelity.mts.application.dto.TransferResponse;
import com.fidelity.mts.service.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
	
	@Autowired
	TransferService transferService;
	
	@PostMapping
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
		TransferResponse response = transferService.transfer(request);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}
