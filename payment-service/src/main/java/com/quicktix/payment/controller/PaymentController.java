package com.quicktix.payment.controller;

import com.quicktix.payment.dto.request.InitiatePaymentRequest;
import com.quicktix.payment.dto.response.ApiResponse;
import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment controller for payment operations
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate a new payment
     * 
     * @param idempotencyKey Unique key to prevent duplicate payments (header)
     * @param request        Payment initiation request
     * @return Payment response with Razorpay order details
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody InitiatePaymentRequest request) {

        log.info("POST /payments/initiate - bookingId={}, idempotencyKey={}",
                request.getBookingId(), idempotencyKey);

        PaymentResponse response = paymentService.initiatePayment(idempotencyKey, request);

        return ResponseEntity.ok(ApiResponse.success("Payment initiated successfully", response));
    }

    /**
     * Get payment by ID
     * 
     * @param paymentId Payment ID
     * @return Payment details
     */
    @GetMapping("/{paymentId:\\d+}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Long paymentId) {

        log.info("GET /payments/{}", paymentId);

        PaymentResponse response = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all payments for a booking
     * 
     * @param bookingId Booking ID
     * @return List of payment responses
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByBooking(
            @PathVariable Long bookingId) {

        log.info("GET /payments/booking/{}", bookingId);

        List<PaymentResponse> payments = paymentService.getPaymentsByBooking(bookingId);

        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
