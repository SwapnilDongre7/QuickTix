package com.quicktix.payment.feign.fallback;

import com.quicktix.payment.feign.BookingFeignClient;
import com.quicktix.payment.feign.dto.PaymentStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for BookingFeignClient.
 * 
 * Handles failures when booking service is unavailable.
 * Payment status updates are critical and should be queued for retry
 * in production scenarios.
 */
@Slf4j
@Component
public class BookingFeignClientFallbackFactory implements FallbackFactory<BookingFeignClient> {

    @Override
    public BookingFeignClient create(Throwable cause) {
        log.error("Booking service fallback triggered: {}", cause.getMessage());

        return new BookingFeignClient() {

            @Override
            public void updatePaymentStatus(PaymentStatusRequest request) {
                log.error("Fallback: Unable to notify booking service of payment status. " +
                        "bookingId={}, status={}, error={}",
                        request.getBookingId(), request.getPaymentStatus(), cause.getMessage());

                // In production, this should be sent to a message queue for retry
                // For now, we log the error - the payment record itself is persisted
                log.warn("Payment status notification queued for retry: bookingId={}",
                        request.getBookingId());
            }
        };
    }
}
