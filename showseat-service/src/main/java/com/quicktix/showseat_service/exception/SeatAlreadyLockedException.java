package com.quicktix.showseat_service.exception;

public class SeatAlreadyLockedException extends RuntimeException {
    private static final long serialVersionUID = 3583077711181551242L;

	public SeatAlreadyLockedException(String message) {
        super(message);
    }
}