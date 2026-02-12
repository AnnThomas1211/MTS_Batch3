package com.fidelity.mts.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelity.mts.application.dto.TransferRequest;
import com.fidelity.mts.application.dto.TransferResponse;
import com.fidelity.mts.domain.enums.Enums.TransactionStatus;
import com.fidelity.mts.domain.exception.AccountNotActiveException;
import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.domain.exception.DuplicateTransferException;
import com.fidelity.mts.domain.exception.InvalidTransferException;
import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.repo.AccountRepository;
import com.fidelity.mts.repo.TransactionLogRepository;

import java.math.BigDecimal;
import java.util.UUID;


@Service
public class TransferServiceImpl implements TransferService {

	
	@Autowired
	AccountRepository accountRepository;
	
	@Autowired
    TransactionLogRepository transactionLogRepository;


    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        validateTransfer(request);
        checkIdempotency(request.idempotencyKey());

        TransactionLog transactionLog = executeTransfer(request);

        return buildSuccessResponse(transactionLog);
    }


    private void validateTransfer(TransferRequest request) {
        if (request.fromAccountId() == request.toAccountId()) {
            throw new InvalidTransferException();
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException();
        }
    }


    private void checkIdempotency(UUID idempotencyKey) {
        transactionLogRepository.findByIdempotencyKey(idempotencyKey)
            .ifPresent(log -> {
                throw new DuplicateTransferException(idempotencyKey);
            });
    }


    private TransactionLog executeTransfer(TransferRequest request) {
        TransactionLog transactionLog = null;
        
        if (request.fromAccountId() == request.toAccountId()) {
        	throw new InvalidTransferException();
        }
        
        try {
            Account fromAccount = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
            
            Account toAccount = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

            if (!fromAccount.isActive()) {
                throw new AccountNotActiveException(request.fromAccountId());
            }
            
            if (!toAccount.isActive()) {
                throw new AccountNotActiveException(request.toAccountId());
            }

            fromAccount.debit(request.amount());

            toAccount.credit(request.amount());

            

            transactionLog = new TransactionLog(
                request.fromAccountId(),
                request.toAccountId(),
                fromAccount.getHolderName(),
                toAccount.getHolderName(),
                request.amount(),
                TransactionStatus.SUCCESS,
                request.idempotencyKey()
            );
            
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            return transactionLogRepository.save(transactionLog);

        } catch (Exception e) {
            transactionLog = new TransactionLog(
                request.fromAccountId(),
                request.toAccountId(),
                "",
                "",
                request.amount(),
                TransactionStatus.FAILED,
                request.idempotencyKey()
            );
            transactionLog.setFailureReason(e.getMessage());
            transactionLogRepository.save(transactionLog);
            
            throw e;
        }
    }

    private TransferResponse buildSuccessResponse(TransactionLog log) {
        return new TransferResponse(
            "TRX-" + log.getId(),
            TransactionStatus.SUCCESS,
            "Transfer completed",
            log.getFromAccountId(),
            log.getToAccountId(),
            log.getFromAccountName(),
            log.getToAccountName(),
            log.getAmount()
        );
    }
}
