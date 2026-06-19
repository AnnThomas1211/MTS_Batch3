package com.fidelity.mts.service;


import com.fidelity.mts.domain.enums.Enums.*;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.domain.model.RewardLedger;
import com.fidelity.mts.repo.RewardLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component("RewardService")
public class RewardService implements RewardServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Minimum amount (exclusive) required for reward eligibility
    private static final BigDecimal REWARD_THRESHOLD = BigDecimal.valueOf(100);
    // One point per this many rupees
    private static final BigDecimal RUPEES_PER_POINT = BigDecimal.valueOf(100);

    private final RewardLedgerRepository rewardLedgerRepository;

    @Autowired
    public RewardService(RewardLedgerRepository rewardLedgerRepository) {
        this.rewardLedgerRepository = rewardLedgerRepository;
    }

    @Override
    @Transactional
    public void evaluateAndGrant(TransactionLog transaction) {
        if (!isEligible(transaction)) {
            logger.info("Transaction {} is not eligible for rewards", transaction.getId());
            return;
        }

        int points = transaction.getAmount().divide(RUPEES_PER_POINT).intValueExact();

        RewardLedger entry = new RewardLedger();
        entry.setAccountId(transaction.getFromAccountId());
        entry.setTransactionId(transaction.getId());
        entry.setPointsAwarded(points);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setDescription("Earned " + points + " point(s) for transferring ₹" + transaction.getAmount()
                + " to account #" + transaction.getToAccountId());

        rewardLedgerRepository.save(entry);
        logger.info("Granted {} reward point(s) to account {} for transaction {}",
                points, transaction.getFromAccountId(), transaction.getId());
    }

    @Override
    public int getRewardBalance(long accountId) {
        return rewardLedgerRepository.sumPointsByAccountId(accountId);
    }

    @Override
    public List<RewardLedger> getRewardHistory(long accountId) {
        return rewardLedgerRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private boolean isEligible(TransactionLog transaction) {
        // Rule 1: must be a successful transaction
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            return false;
        }
        // Rule 2: amount must be greater than ₹100
        if (transaction.getAmount().compareTo(REWARD_THRESHOLD) <= 0) {
            return false;
        }
        // Rules 3 & 4: sender and receiver must be different accounts
        if (transaction.getFromAccountId() == transaction.getToAccountId()) {
            return false;
        }
        return true;
    }
}
