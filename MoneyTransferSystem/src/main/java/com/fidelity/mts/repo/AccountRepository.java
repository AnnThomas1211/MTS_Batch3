package com.fidelity.mts.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fidelity.mts.domain.model.Account;


public interface AccountRepository extends JpaRepository<Account, Long> {
	
}
