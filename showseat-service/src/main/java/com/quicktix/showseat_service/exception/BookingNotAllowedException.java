package com.quicktix.showseat_service.exception;

public class BookingNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = -1505137316594362808L;

	public BookingNotAllowedException(String message) {
        super(message);
    }
}