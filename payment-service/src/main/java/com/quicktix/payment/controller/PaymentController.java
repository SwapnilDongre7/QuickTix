package com.quicktix.payment.controller;

import com.quicktix.payment.dto.request.InitiatePaymentRequest;
import com.quicktix.payment.dto.request.FailPaymentRequest;
import com.quicktix.payment.dto.request.VerifyPaymentRequest;
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

    /**
     * Verify payment after Razorpay checkout completion.
     * This endpoint is called by the frontend after the user completes payment on
     * Razorpay.
     * It validates the signature and updates the payment status.
     * 
     * @param request Verification request with Razorpay payment details
     * @return Payment response with updated status
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        log.info("POST /payments/verify - orderId={}", request.getRazorpayOrderId());

        PaymentResponse response = paymentService.verifyPayment(request);

        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    /**
     * Handle payment failure or cancellation.
     * Called by frontend when user cancels Razorpay checkout or payment fails.
     * This will cancel the booking and release locked seats.
     * 
     * @param request Failure request with booking ID and reason
     * @return Success acknowledgment
     */
    @PostMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> failPayment(
            @Valid @RequestBody FailPaymentRequest request) {

        log.info("POST /payments/fail - bookingId={}, reason={}",
                request.getBookingId(), request.getFailureReason());

        paymentService.failPayment(request.getBookingId(), request.getFailureReason());

        return ResponseEntity.ok(ApiResponse.success("Payment failure recorded", null));
    }
}
