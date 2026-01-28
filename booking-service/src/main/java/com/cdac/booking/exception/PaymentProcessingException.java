package com.cdac.booking.exception;

public class PaymentProcessingException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentProcessingException(String message) {
        super(message);
    }
}