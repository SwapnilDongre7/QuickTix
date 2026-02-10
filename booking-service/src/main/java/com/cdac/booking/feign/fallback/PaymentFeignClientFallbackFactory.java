package com.cdac.booking.feign.fallback;

import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.feign.PaymentFeignClient;
import com.cdac.booking.feign.dto.InitiatePaymentRequest;
import com.cdac.booking.feign.dto.PaymentResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for PaymentFeignClient.
 * 
 * Provides error handling when Payment service is unavailable.
 * Payment failures are critical and should be communicated clearly to the user.
 */
@Slf4j
@Component
public class PaymentFeignClientFallbackFactory implements FallbackFactory<PaymentFeignClient> {

    @Override
    public PaymentFeignClient create(Throwable cause) {
        // Log the full exception for debugging
        log.error("Payment service fallback triggered. Root cause:", cause);

        return new PaymentFeignClient() {

            @Override
            public ApiResponseDto<PaymentResponseDto> initiatePayment(
                    String idempotencyKey, InitiatePaymentRequest request) {
                String errorDetails = cause.getMessage();
                if (cause.getCause() != null) {
                    errorDetails += " | Caused by: " + cause.getCause().getMessage();
                }
                log.error("Fallback: Unable to initiate payment for booking {}. Error: {}",
                        request.getBookingId(), errorDetails, cause);
                throw new RuntimeException(
                        "Payment service temporarily unavailable: " + errorDetails);
            }
        };
    }
}
