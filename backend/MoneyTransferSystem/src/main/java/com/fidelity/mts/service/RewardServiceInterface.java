package com.fidelity.mts.service;

import com.fidelity.mts.domain.model.RewardLedger;
import com.fidelity.mts.domain.model.TransactionLog;

import java.util.List;

public interface RewardServiceInterface {

    void evaluateAndGrant(TransactionLog transaction);
    int getRewardBalance(long accountId);
    List<RewardLedger> getRewardHistory(long accountId);

    // Add this new method
    int redeemPoints(long accountId);

}