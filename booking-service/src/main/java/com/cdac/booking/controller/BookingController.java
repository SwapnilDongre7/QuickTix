package com.cdac.booking.controller;

import com.cdac.booking.dto.BookingDetailsResponse;
import com.cdac.booking.dto.CreateBookingRequest;
import com.cdac.booking.dto.CreateBookingResponse;
import com.cdac.booking.dto.TicketResponse;
import com.cdac.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	/**
	 * Create a new booking
	 */
	@PostMapping
	public ResponseEntity<CreateBookingResponse> createBooking(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody CreateBookingRequest request) {

		log.info("POST /bookings - showId={}, userId={}", request.getShowId(), request.getUserId());
		return ResponseEntity.ok(bookingService.createBooking(idempotencyKey, request));
	}

	/**
	 * Get booking by ID
	 * Used by frontend to display booking/ticket details
	 */
	@GetMapping("/{bookingId}")
	public ResponseEntity<BookingDetailsResponse> getBooking(@PathVariable Long bookingId) {
		log.info("GET /bookings/{}", bookingId);
		return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
	}

	/**
	 * Get enriched ticket by booking ID
	 * Returns fully enriched ticket data with movie name, theatre name, screen
	 * name, etc.
	 * Frontend can render the ticket directly without additional API calls.
	 */
	@GetMapping("/{bookingId}/ticket")
	public ResponseEntity<TicketResponse> getTicket(@PathVariable Long bookingId) {
		log.info("GET /bookings/{}/ticket", bookingId);
		return ResponseEntity.ok(bookingService.getTicket(bookingId));
	}

	/**
	 * Get seat numbers for a booking
	 * Used by frontend to display booked seats
	 */
	@GetMapping("/{bookingId}/seats")
	public ResponseEntity<List<String>> getBookingSeats(@PathVariable Long bookingId) {
		log.info("GET /bookings/{}/seats", bookingId);
		return ResponseEntity.ok(bookingService.getBookingSeats(bookingId));
	}

	/**
	 * Get all bookings for a user
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookingDetailsResponse>> getUserBookings(@PathVariable Long userId) {
		log.info("GET /bookings/user/{}", userId);
		return ResponseEntity.ok(bookingService.getUserBookings(userId));
	}
}