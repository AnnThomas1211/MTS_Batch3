package com.fidelity.mts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fidelity.mts.application.dto.TransferRequest;
import com.fidelity.mts.application.dto.TransferResponse;
import com.fidelity.mts.repo.TransactionLogRepository;

@Service
public class TransferServiceImpl implements TransferService{

	@Autowired
	AccountService accountService;
	
	@Autowired
	TransactionLogRepository transactionLogRepository;
	
	@Override
	public TransferResponse transfer(TransferRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
