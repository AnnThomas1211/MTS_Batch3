package com.fidelity.mts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fidelity.mts.domain.enums.Enums.AccountStatus;
import com.fidelity.mts.domain.exception.AccountNotActiveException;
import com.fidelity.mts.domain.exception.InsufficientBalanceException;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
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

    @NotNull
	@Column(name = "password_hash", length = 100)
	private String passwordHash;

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


}
