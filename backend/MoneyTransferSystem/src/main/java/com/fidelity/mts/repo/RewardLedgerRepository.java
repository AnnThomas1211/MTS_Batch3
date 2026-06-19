package com.fidelity.mts.repo;

import com.fidelity.mts.domain.model.RewardLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RewardLedgerRepository extends JpaRepository<RewardLedger, Long> {
    List<RewardLedger> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardLedger r WHERE r.accountId = :accountId")
    int sumPointsByAccountId(@Param("accountId") Long accountId);
}
