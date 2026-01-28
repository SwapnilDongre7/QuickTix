package com.cdac.booking.exception;

public class BookingNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BookingNotFoundException(Long bookingId) {
		super("Booking not found with id: " + bookingId);
	}
}