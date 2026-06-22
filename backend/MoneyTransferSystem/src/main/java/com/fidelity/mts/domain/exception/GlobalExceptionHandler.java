package com.fidelity.mts.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e) {
		ErrorResponse res = new ErrorResponse("AUTH-401", e.getMessage());
		return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		// Surface the first field error message so the client gets a precise,
		// human-readable reason (e.g. "Account ID must be a positive number.").
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("Invalid request. Please check the submitted fields.");
		ErrorResponse res = new ErrorResponse("VAL-400", message);
		return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
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
		
		return new ResponseEntity<>(res, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	
	
}
