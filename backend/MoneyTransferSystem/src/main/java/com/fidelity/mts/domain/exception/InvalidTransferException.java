package com.fidelity.mts.domain.exception;

@SuppressWarnings("serial")
public class InvalidTransferException extends RuntimeException {
	public InvalidTransferException() {
		super("Illegal Transfer operation : Sender and receiver accounts cannot be the same.");
	}
}

