package com.quicktix.showseat_service.exception;

public class SeatAlreadyBookedException extends RuntimeException {
    private static final long serialVersionUID = 837914731945825720L;

	public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
