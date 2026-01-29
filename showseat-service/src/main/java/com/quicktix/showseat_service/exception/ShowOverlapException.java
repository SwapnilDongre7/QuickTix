package com.quicktix.showseat_service.exception;

public class ShowOverlapException extends RuntimeException {
    private static final long serialVersionUID = 633480195363546505L;

	public ShowOverlapException(String message) {
        super(message);
    }
}