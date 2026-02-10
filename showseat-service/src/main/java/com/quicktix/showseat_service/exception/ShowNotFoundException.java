package com.quicktix.showseat_service.exception;

public class ShowNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 4275303938717500700L;

	public ShowNotFoundException(String message) {
        super(message);
    }
}