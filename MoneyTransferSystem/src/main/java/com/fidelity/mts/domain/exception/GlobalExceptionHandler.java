package com.fidelity.mts.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fidelity.mts.application.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException e) {
		ErrorResponse res = new ErrorResponse("ACC-404", e.getMessage());
		return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(AccountNotActiveException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotActiveException(AccountNotActiveException e) {
		ErrorResponse res = new ErrorResponse("ACC-403", e.getMessage());
		
		return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
		ErrorResponse res = new ErrorResponse("TRX-400", e.getMessage());
		
		return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(DuplicateTransferException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateTransferException(DuplicateTransferException e) {
		ErrorResponse res = new ErrorResponse("TRX-409", e.getMessage());
		
		return new ResponseEntity<>(res, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(InvalidTransferException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransferException(InvalidTransferException e) {
		ErrorResponse res = new ErrorResponse("VAL-422", e.getMessage());
		
		return new ResponseEntity<>(res, HttpStatus.CONFLICT);
	}
	
	
	
}
