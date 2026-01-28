package com.cdac.booking.controller;

import com.cdac.booking.dto.CreateBookingRequest;
import com.cdac.booking.dto.CreateBookingResponse;
import com.cdac.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	@PostMapping
	public ResponseEntity<CreateBookingResponse> createBooking(@RequestHeader("Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody CreateBookingRequest request) {

		return ResponseEntity.ok(bookingService.createBooking(idempotencyKey, request));
	}
}