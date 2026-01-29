package com.cdac.booking.exception;

public class DuplicateBookingException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateBookingException(String idempotencyKey) {
        super("Duplicate booking request with idempotency key: " + idempotencyKey);
    }
}