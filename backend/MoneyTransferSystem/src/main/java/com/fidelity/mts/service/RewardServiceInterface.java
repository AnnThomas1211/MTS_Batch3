package com.fidelity.mts.service;

import com.fidelity.mts.domain.model.RewardLedger;
import com.fidelity.mts.domain.model.TransactionLog;



import java.util.List;

public interface RewardServiceInterface {

    /**
     * Evaluates reward eligibility for a completed transaction
     * and persists a RewardLedger entry when eligible.
     *
     * Eligibility rules:
     *   1. Transaction status is SUCCESS
     *   2. Amount > 100
     *   3. Sender and receiver are different accounts (no self-transfer)
     *
     * Points formula: floor(amount / 100)
     */
    void evaluateAndGrant(TransactionLog transaction);

    /** Total accumulated reward points for an account. */
    int getRewardBalance(long accountId);

    /** Full reward history for an account, newest first. */
    List<RewardLedger> getRewardHistory(long accountId);
}
