package com.fidelity.mts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.exception.AccountNotActiveException;
import com.fidelity.mts.domain.exception.InsufficientBalanceException;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "accounts")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@NotNull
	@Column(name = "holder_name")
	private String holderName;
	
	@NotNull
	@Column(name = "balance", precision = 18)
	private BigDecimal balance;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private AccountStatus status;
	
	@Version
	@Column(name = "version")
	private int version = 0;
	
	@Column(name = "last_updated")
	private LocalDateTime lastUpdated;

	public Account() {
		
	}
	public Account(long id, @NotNull String holderName, @NotNull BigDecimal balance, @NotNull AccountStatus status,
			int version, LocalDateTime lastUpdated) {
		super();
		this.id = id;
		this.holderName = holderName;
		this.balance = balance;
		this.status = status;
		this.version = version;
		this.lastUpdated = lastUpdated;
	}
	
	public boolean isActive() {
		return getStatus().equals(AccountStatus.ACTIVE);
	}
	
	public void debit(BigDecimal amount) {
		if (!this.isActive()) {
			throw new AccountNotActiveException(this.id);
		}
		
		if (this.balance.compareTo(amount) < 0) {
			throw new InsufficientBalanceException(this.id, amount, balance);
		}
		
		this.balance = this.balance.subtract(amount);
		this.lastUpdated = LocalDateTime.now();
	}
	
	public void credit(BigDecimal amount) {
		if (!this.isActive()) {
			throw new AccountNotActiveException(this.id);
		}
		
		this.balance = this.balance.add(amount);
		this.lastUpdated = LocalDateTime.now();
				
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getHolderName() {
		return holderName;
	}
	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public AccountStatus getStatus() {
		return status;
	}
	public void setStatus(AccountStatus status) {
		this.status = status;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	
}
