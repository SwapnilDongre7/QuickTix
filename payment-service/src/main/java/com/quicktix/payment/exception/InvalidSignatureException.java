package com.quicktix.payment.exception;

/**
 * Exception thrown when webhook signature verification fails
 */
public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException(String message) {
        super(message);
    }

    public InvalidSignatureException() {
        super("Invalid webhook signature - potential fraud attempt");
    }
}
