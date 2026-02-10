package com.cdac.booking.client;

import java.math.BigDecimal;

/**
 * Payment client interface for initiating payments
 */
public interface PaymentClientInterface {

    /**
     * Initiate a payment for a booking
     * 
     * @param bookingId      Booking ID
     * @param userId         User ID
     * @param amount         Amount to charge
     * @param idempotencyKey Unique key to prevent duplicate payments
     * @return Payment response with payment URL
     */
    PaymentResponse initiatePayment(Long bookingId, Long userId, BigDecimal amount, String idempotencyKey);

    /**
     * Payment response DTO
     */
    record PaymentResponse(
            Long paymentId,
            Long bookingId,
            String razorpayOrderId,
            String paymentUrl,
            String status) {
    }
}
