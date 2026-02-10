package com.cdac.booking.controller;

import com.cdac.booking.dto.PaymentStatusUpdateRequest;
import com.cdac.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingPaymentController {

	private final BookingService bookingService;

	@PostMapping("/payment-status")
	public ResponseEntity<Void> updatePaymentStatus(@RequestBody PaymentStatusUpdateRequest request) {

		bookingService.handlePaymentUpdate(request.getBookingId(), request.getPaymentStatus(),
				request.getPaymentId(), request.getFailureReason());

		return ResponseEntity.ok().build();
	}
}