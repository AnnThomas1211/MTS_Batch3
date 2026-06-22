package com.fidelity.mts.service;

import com.fidelity.mts.domain.enums.Enums.*;
import com.fidelity.mts.domain.exception.AccountNotFoundException;
import com.fidelity.mts.domain.model.Account;
import com.fidelity.mts.domain.model.TransactionLog;
import com.fidelity.mts.domain.model.RewardLedger;
import com.fidelity.mts.repo.AccountRepository;
import com.fidelity.mts.repo.RewardLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component("RewardService")
public class RewardService implements RewardServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);

    private static final BigDecimal REWARD_THRESHOLD = BigDecimal.valueOf(100);
    private static final BigDecimal RUPEES_PER_POINT = BigDecimal.valueOf(100);

    private final RewardLedgerRepository rewardLedgerRepository;

    // Inject AccountRepository to update the account balance
    private final AccountRepository accountRepository;

    @Autowired
    public RewardService(RewardLedgerRepository rewardLedgerRepository, AccountRepository accountRepository) {
        this.rewardLedgerRepository = rewardLedgerRepository;
        this.accountRepository = accountRepository;
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

    // --- NEW REDEEM METHOD ---
    @Override
    @Transactional
    public int redeemPoints(long accountId) {
        int totalPoints = getRewardBalance(accountId);

        if (totalPoints <= 0) {
            throw new IllegalArgumentException("No reward points available to redeem.");
        }

        // 1. Fetch the account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 2. Add cash to balance (1 point = ₹1) using your existing credit method
        BigDecimal cashAmount = BigDecimal.valueOf(totalPoints);
        account.credit(cashAmount);
        accountRepository.save(account);

        // 3. Create a negative ledger entry to zero out the points balance while keeping history
        RewardLedger redemptionEntry = new RewardLedger();
        redemptionEntry.setAccountId(accountId);
        redemptionEntry.setTransactionId(UUID.randomUUID()); // 0 or null to indicate system transaction
        redemptionEntry.setPointsAwarded(-totalPoints); // Negative points to balance it to 0
        redemptionEntry.setCreatedAt(LocalDateTime.now());
        redemptionEntry.setDescription("Redeemed " + totalPoints + " point(s) for ₹" + cashAmount);

        rewardLedgerRepository.save(redemptionEntry);

        logger.info("Redeemed {} point(s) for ₹{} for account {}", totalPoints, cashAmount, accountId);

        return totalPoints;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private boolean isEligible(TransactionLog transaction) {
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            return false;
        }
        if (transaction.getAmount().compareTo(REWARD_THRESHOLD) <= 0) {
            return false;
        }
        if (transaction.getFromAccountId() == transaction.getToAccountId()) {
            return false;
        }
        return true;
    }
}