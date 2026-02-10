package com.cdac.booking.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// -------------------------
	// SEAT LOCK FAILURE
	// -------------------------
	@ExceptionHandler(SeatLockFailedException.class)
	public ResponseEntity<Map<String, Object>> handleSeatLock(SeatLockFailedException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage());
	}

	// -------------------------
	// BOOKING NOT FOUND
	// -------------------------
	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleBookingNotFound(BookingNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	// -------------------------
	// DUPLICATE / IDEMPOTENCY
	// -------------------------
	@ExceptionHandler(DuplicateBookingException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateBookingException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage());
	}

	// -------------------------
	// INVALID STATE
	// -------------------------
	@ExceptionHandler(InvalidBookingStateException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidBookingStateException ex) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	// -------------------------
	// PAYMENT FAILURE
	// -------------------------
	@ExceptionHandler(PaymentProcessingException.class)
	public ResponseEntity<Map<String, Object>> handlePayment(PaymentProcessingException ex) {
		return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage());
	}

	// -------------------------
	// BAD REQUEST / VALIDATION
	// -------------------------
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		return buildError(HttpStatus.METHOD_NOT_ALLOWED, "Request method '" + ex.getMethod() + "' is not supported");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
		return buildError(HttpStatus.BAD_REQUEST, "Malformed JSON request or missing body");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Validation Error");
		body.put("message", "Input validation failed");
		body.put("details", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// -------------------------
	// FALLBACK (VERY IMPORTANT)
	// -------------------------
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
		ex.printStackTrace(); // Log stack trace for debugging
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage());
	}

	// -------------------------
	// COMMON RESPONSE BUILDER
	// -------------------------
	private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);

		return ResponseEntity.status(status).body(body);
	}
}