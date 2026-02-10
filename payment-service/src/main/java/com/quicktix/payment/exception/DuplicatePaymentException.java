package com.quicktix.payment.exception;

/**
 * Exception thrown for duplicate payment attempts
 */
public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
