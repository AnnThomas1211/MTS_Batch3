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

/**
 * Service class for handling money transfer operations.
 * Implements the core business logic for fund transfers with ACID compliance.
 */
@Service
public class TransferServiceImpl implements TransferService {

	
	@Autowired
	AccountRepository accountRepository;
	
	@Autowired
    TransactionLogRepository transactionLogRepository;


    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Step 1: Validate the transfer request
        validateTransfer(request);

        // Step 2: Check for duplicate transfer (idempotency)
        checkIdempotency(request.idempotencyKey());

        // Step 3: Execute the transfer
        TransactionLog transactionLog = executeTransfer(request);

        // Step 4: Return response
        return buildSuccessResponse(transactionLog);
    }

    /**
     * Validates the transfer request.
     * 
     * @param request the transfer request
     * @throws InvalidTransferException if validation fails
     */
    private void validateTransfer(TransferRequest request) {
        // Rule 1: Accounts must be different
        if (request.fromAccountId() == request.toAccountId()) {
            throw new InvalidTransferException();
        }

        // Rule 6: Amount must be > 0
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
        
        try {
            // Step 1: Fetch and lock accounts
            Account fromAccount = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
            
            Account toAccount = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

            // Step 2: Validate account statuses
            if (!fromAccount.isActive()) {
                throw new AccountNotActiveException(request.fromAccountId());
            }
            
            if (!toAccount.isActive()) {
                throw new AccountNotActiveException(request.toAccountId());
            }

            // Step 3: Debit from source account (will throw InsufficientBalanceException if needed)
            fromAccount.debit(request.amount());

            // Step 4: Credit to destination account
            toAccount.credit(request.amount());

            // Step 5: Save accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Step 6: Create successful transaction log
            transactionLog = new TransactionLog(
                request.fromAccountId(),
                request.fromAccountId(),
                request.amount(),
                TransactionStatus.SUCCESS,
                request.idempotencyKey()
            );
            
            return transactionLogRepository.save(transactionLog);

        } catch (Exception e) {
            // Log failed transaction
            transactionLog = new TransactionLog(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                TransactionStatus.FAILED,
                request.idempotencyKey()
            );
            transactionLog.setFailureReason(e.getMessage());
            transactionLogRepository.save(transactionLog);
            
            // Re-throw the exception
            throw e;
        }
    }

    /**
     * Builds a success response from a transaction log.
     * 
     * @param log the transaction log
     * @return the transfer response
     */
    private TransferResponse buildSuccessResponse(TransactionLog log) {
        return new TransferResponse(
            "TRX-" + log.getId(),
            "SUCCESS",
            "Transfer completed",
            log.getFromAccountId(),
            log.getToAccountId(),
            log.getAmount()
        );
    }
}
