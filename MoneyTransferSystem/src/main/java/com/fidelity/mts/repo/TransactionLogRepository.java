package com.fidelity.mts.repo;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fidelity.mts.domain.model.TransactionLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    // Transaction history for an account (as sender or receiver)
    List<TransactionLog> findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(Long fromAccountId, Long toAccountId);
}