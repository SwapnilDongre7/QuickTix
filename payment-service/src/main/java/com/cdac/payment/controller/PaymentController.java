package com.cdac.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.payment.dto.PaymentRequest;
import com.cdac.payment.entity.Payment;
import com.cdac.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/initiate")
	public ResponseEntity<Payment> initiatePayment(@RequestHeader("Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody PaymentRequest request) {

		Payment payment = paymentService.createPayment(idempotencyKey, request.getBookingId(), request.getUserId(),
				request.getAmount(), request.getPaymentMode());

		return ResponseEntity.ok(payment);
	}

}
