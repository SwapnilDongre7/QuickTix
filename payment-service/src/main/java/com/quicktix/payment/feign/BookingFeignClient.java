package com.quicktix.payment.feign;

import com.quicktix.payment.feign.config.FeignConfig;
import com.quicktix.payment.feign.dto.PaymentStatusRequest;
import com.quicktix.payment.feign.fallback.BookingFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Booking Service.
 * 
 * Provides declarative REST client for notifying booking service
 * about payment status changes.
 * Uses Eureka service discovery via service name "BOOKING-SERVICE".
 */
@FeignClient(name = "BOOKING-SERVICE", configuration = FeignConfig.class, fallbackFactory = BookingFeignClientFallbackFactory.class)
public interface BookingFeignClient {

    /**
     * Notify booking service of payment status update.
     *
     * @param request Payment status update request
     */
    @PostMapping("/bookings/payment-status")
    void updatePaymentStatus(@RequestBody PaymentStatusRequest request);
}
