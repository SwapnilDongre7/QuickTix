package com.quicktix.payment.service.impl;

import com.quicktix.payment.feign.BookingFeignClient;
import com.quicktix.payment.feign.dto.PaymentStatusRequest;
import com.quicktix.payment.service.BookingNotificationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of BookingNotificationService using OpenFeign.
 * 
 * Uses declarative Feign client for cleaner, service discovery-based
 * communication with the booking service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingNotificationServiceImpl implements BookingNotificationService {

    private final BookingFeignClient bookingFeignClient;

    @Override
    @CircuitBreaker(name = "bookingService", fallbackMethod = "notifySuccessFallback")
    @Retry(name = "bookingService")
    public void notifyPaymentSuccess(Long bookingId, Long paymentId) {
        log.info("Notifying booking service of payment success via Feign: bookingId={}, paymentId={}",
                bookingId, paymentId);

        PaymentStatusRequest request = PaymentStatusRequest.builder()
                .bookingId(bookingId)
                .paymentStatus("SUCCESS")
                .paymentId(paymentId)
                .build();

        bookingFeignClient.updatePaymentStatus(request);

        log.info("Successfully notified booking service of payment success");
    }

    @Override
    @CircuitBreaker(name = "bookingService", fallbackMethod = "notifyFailureFallback")
    @Retry(name = "bookingService")
    public void notifyPaymentFailure(Long bookingId, Long paymentId, String reason) {
        log.info("Notifying booking service of payment failure via Feign: bookingId={}, paymentId={}",
                bookingId, paymentId);

        PaymentStatusRequest request = PaymentStatusRequest.builder()
                .bookingId(bookingId)
                .paymentStatus("FAILED")
                .paymentId(paymentId)
                .failureReason(reason)
                .build();

        bookingFeignClient.updatePaymentStatus(request);

        log.info("Successfully notified booking service of payment failure");
    }

    // Fallback methods for circuit breaker
    private void notifySuccessFallback(Long bookingId, Long paymentId, Exception ex) {
        log.error("Failed to notify booking service of success after retries. bookingId={}, error={}",
                bookingId, ex.getMessage());
        // In production, this should be sent to a message queue for retry
    }

    private void notifyFailureFallback(Long bookingId, Long paymentId, String reason, Exception ex) {
        log.error("Failed to notify booking service of failure after retries. bookingId={}, error={}",
                bookingId, ex.getMessage());
        // In production, this should be sent to a message queue for retry
    }
}
