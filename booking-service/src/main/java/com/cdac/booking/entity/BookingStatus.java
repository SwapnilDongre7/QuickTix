package com.cdac.booking.entity;

/**
 * Booking status enumeration
 */
public enum BookingStatus {
	INITIATED, // Booking created, seats locked, waiting for payment
	CONFIRMED, // Payment successful, booking complete
	CANCELLED, // Booking cancelled by user or payment failed
	EXPIRED // Booking expired due to payment timeout (added for cleanup)
}