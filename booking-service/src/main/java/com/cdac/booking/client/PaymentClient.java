package com.cdac.booking.client;

import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.feign.PaymentFeignClient;
import com.cdac.booking.feign.dto.InitiatePaymentRequest;
import com.cdac.booking.feign.dto.PaymentResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Payment client using OpenFeign with Eureka service discovery.
 * 
 * This client communicates with the Payment Service to initiate payments.
 * Circuit breaker prevents cascading failures when payment service is down.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient implements PaymentClientInterface {

	private final PaymentFeignClient paymentFeignClient;

	@Override
	@CircuitBreaker(name = "paymentService", fallbackMethod = "initiatePaymentFallback")
	@Retry(name = "paymentService")
	public PaymentResponse initiatePayment(Long bookingId, Long userId, BigDecimal amount, String idempotencyKey) {
		log.info("Initiating payment via Feign: bookingId={}, amount={}", bookingId, amount);

		InitiatePaymentRequest request = InitiatePaymentRequest.builder()
				.bookingId(bookingId)
				.userId(userId)
				.amount(amount)
				.currency("INR")
				.description("QuickTix Movie Booking #" + bookingId)
				.build();

		ApiResponseDto<PaymentResponseDto> response = paymentFeignClient.initiatePayment(idempotencyKey, request);

		if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getData() != null) {
			PaymentResponseDto data = response.getData();
			log.info("Payment initiated successfully: paymentId={}", data.getPaymentId());
			return new PaymentResponse(
					data.getPaymentId(),
					data.getBookingId(),
					data.getRazorpayOrderId(),
					data.getPaymentUrl(),
					data.getStatus());
		}

		String errorMsg = response != null ? response.getError() : "Empty response";
		log.error("Payment initiation failed: {}", errorMsg);
		throw new RuntimeException("Payment initiation failed: " + errorMsg);
	}

	/**
	 * Fallback method when payment service is unavailable
	 */
	private PaymentResponse initiatePaymentFallback(Long bookingId, Long userId,
			BigDecimal amount, String idempotencyKey, Exception ex) {
		log.error("Payment service unavailable for booking {}. Error: {}", bookingId, ex.getMessage());
		throw new RuntimeException("Payment service temporarily unavailable. Please try again later.");
	}
}