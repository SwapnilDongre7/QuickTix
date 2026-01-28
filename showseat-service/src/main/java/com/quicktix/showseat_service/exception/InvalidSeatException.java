package com.quicktix.showseat_service.exception;

public class InvalidSeatException extends RuntimeException {
    private static final long serialVersionUID = -1638116899797453973L;

	public InvalidSeatException(String message) {
        super(message);
    }
}