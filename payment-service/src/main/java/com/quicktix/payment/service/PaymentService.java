package com.quicktix.payment.service;

import com.quicktix.payment.dto.request.InitiatePaymentRequest;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.dto.request.VerifyPaymentRequest;
import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.entity.Payment;

import java.util.List;

/**
 * Payment service interface
 */
public interface PaymentService {

    /**
     * Initiate a new payment
     * 
     * @param idempotencyKey Unique key to prevent duplicate payments
     * @param request        Payment initiation request
     * @return Payment response with Razorpay order details
     */
    PaymentResponse initiatePayment(String idempotencyKey, InitiatePaymentRequest request);

    /**
     * Process webhook callback from Razorpay
     * 
     * @param payload   Raw webhook payload for signature verification
     * @param signature X-Razorpay-Signature header value
     * @param event     Parsed webhook event
     */
    void processWebhook(String payload, String signature, RazorpayWebhookEvent event);

    /**
     * Get payment by ID
     * 
     * @param paymentId Payment ID
     * @return Payment response
     */
    PaymentResponse getPayment(Long paymentId);

    /**
     * Get all payments for a booking
     * 
     * @param bookingId Booking ID
     * @return List of payment responses
     */
    List<PaymentResponse> getPaymentsByBooking(Long bookingId);

    /**
     * Get payment by Razorpay order ID
     * 
     * @param orderId Razorpay order ID
     * @return Payment entity
     */
    Payment getPaymentByRazorpayOrderId(String orderId);

    /**
     * Verify payment after Razorpay checkout completion.
     * Validates the signature and updates payment status.
     * 
     * @param request Verification request with Razorpay payment details
     * @return Payment response with updated status
     */
    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    /**
     * Mark payment as failed and notify booking service.
     * Called by frontend when user cancels Razorpay checkout.
     * 
     * @param bookingId     Booking ID
     * @param failureReason Reason for failure
     */
    void failPayment(Long bookingId, String failureReason);
}
