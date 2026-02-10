package com.quicktix.payment.service;

/**
 * Service for notifying booking service about payment status
 */
public interface BookingNotificationService {

    /**
     * Notify booking service about payment success
     * 
     * @param bookingId Booking ID
     * @param paymentId Payment ID
     */
    void notifyPaymentSuccess(Long bookingId, Long paymentId);

    /**
     * Notify booking service about payment failure
     * 
     * @param bookingId Booking ID
     * @param paymentId Payment ID
     * @param reason    Failure reason
     */
    void notifyPaymentFailure(Long bookingId, Long paymentId, String reason);
}
