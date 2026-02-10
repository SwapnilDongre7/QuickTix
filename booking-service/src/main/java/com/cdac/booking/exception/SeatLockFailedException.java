package com.cdac.booking.exception;

public class SeatLockFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SeatLockFailedException(String message) {
		super(message);
	}
}