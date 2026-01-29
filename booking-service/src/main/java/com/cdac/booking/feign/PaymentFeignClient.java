package com.cdac.booking.feign;

import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.feign.config.FeignConfig;
import com.cdac.booking.feign.dto.InitiatePaymentRequest;
import com.cdac.booking.feign.dto.PaymentResponseDto;
import com.cdac.booking.feign.fallback.PaymentFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Payment Service.
 * 
 * Provides declarative REST client for payment operations.
 * Uses Eureka service discovery via service name "PAYMENT-SERVICE".
 */
@FeignClient(name = "PAYMENT-SERVICE", configuration = FeignConfig.class, fallbackFactory = PaymentFeignClientFallbackFactory.class)
public interface PaymentFeignClient {

    /**
     * Initiate a new payment for a booking.
     *
     * @param idempotencyKey Unique key to prevent duplicate payments
     * @param request        Payment initiation request
     * @return Payment response with Razorpay order details
     */
    @PostMapping("/payments/initiate")
    ApiResponseDto<PaymentResponseDto> initiatePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody InitiatePaymentRequest request);
}
