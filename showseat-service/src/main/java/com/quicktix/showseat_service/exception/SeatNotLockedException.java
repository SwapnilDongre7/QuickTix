package com.quicktix.showseat_service.exception;

public class SeatNotLockedException extends RuntimeException {
    private static final long serialVersionUID = 2761975041940162729L;

	public SeatNotLockedException(String message) {
        super(message);
    }
}
