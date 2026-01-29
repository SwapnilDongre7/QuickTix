package com.quicktix.showseat_service.exception;

public class LayoutNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1475089643816438773L;

	public LayoutNotFoundException(String message) {
        super(message);
    }
}