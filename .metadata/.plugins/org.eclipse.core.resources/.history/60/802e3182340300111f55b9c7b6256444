package com.fidelity.mts.domain.model;

import java.sql.Timestamp;

import com.fidelity.mts.domain.enums.Enums.TransactionStatus;

public class TransactionLog {
	private long id;
	private long fromAccountId;
	private long toAccountId;
	private long amount;
	private TransactionStatus status;
	private String failureReason;
	private String idempotencyKey;
	private Timestamp createdOn;
	public TransactionLog(long id, long fromAccountId, long toAccountId, long amount, TransactionStatus status,
			String failureReason, String idempotencyKey, Timestamp createdOn) {
		super();
		this.id = id;
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.status = status;
		this.failureReason = failureReason;
		this.idempotencyKey = idempotencyKey;
		this.createdOn = createdOn;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
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
	public Timestamp getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}
	
}
