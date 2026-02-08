package com.fidelity.mts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "from_account")
	private long fromAccountId;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "to_account")
	private long toAccountId;
	
	@NotNull
	@Column(name = "amount", precision = 18)
	private BigDecimal amount;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private TransactionStatus status;
	
	@Column(name = "failure_reason")
	private String failureReason;
	
	@Column(name = "idempotency_key", unique = true, length = 100)
	private String idempotencyKey;
	
	@Column(name = "created_on")
	private LocalDateTime createdOn;
	
	public TransactionLog() {}
	public TransactionLog(long fromAccountId, long toAccountId, BigDecimal amount, TransactionStatus status,
			String failureReason, String idempotencyKey) {
		this.id = UUID.randomUUID();
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.status = status;
		this.failureReason = failureReason;
		this.idempotencyKey = idempotencyKey;
		this.createdOn = LocalDateTime.now();
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public long getFromAccountId() {
		return fromAccountId;
	}
	public void setFromAccountId(long fromAccountId) {
		this.fromAccountId = fromAccountId;
	}
	public long getToAccountId() {
		return toAccountId;
	}
	public void setToAccountId(long toAccountId) {
		this.toAccountId = toAccountId;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public TransactionStatus getStatus() {
		return status;
	}
	public void setStatus(TransactionStatus status) {
		this.status = status;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
	public String getIdempotencyKey() {
		return idempotencyKey;
	}
	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}
	
}
