package com.cdac.booking.client;

import com.cdac.booking.dto.seat.*;
import com.cdac.booking.exception.SeatLockFailedException;
import com.cdac.booking.feign.ShowSeatFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SeatClient implementation using OpenFeign with Eureka service discovery.
 * 
 * This implementation replaces the RestTemplate-based approach with
 * declarative Feign clients for cleaner, more maintainable code.
 * 
 * Features:
 * - Eureka-based service discovery (no hardcoded URLs)
 * - Automatic JWT token propagation
 * - Circuit breaker with fallback
 * - Retry mechanism
 */
@Slf4j
@Component
@Profile("!local-mock-seat")
@RequiredArgsConstructor
public class SeatClientImpl implements SeatClient {

	private final ShowSeatFeignClient showSeatFeignClient;

	@Override
	@CircuitBreaker(name = "seatService", fallbackMethod = "lockSeatsFallback")
	@Retry(name = "seatService")
	public LockSeatsResponseDto lockSeats(LockSeatsRequestDto request) {
		log.info("Locking seats via Feign: showId={}, seats={}",
				request.getShowId(), request.getSeatNumbers());

		ApiResponseDto<LockSeatsResponseDto> response = showSeatFeignClient.lockSeats(request);

		if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
			String errorMsg = response != null ? response.getError() : "Empty response";
			log.error("Seat locking failed: {}", errorMsg);
			throw new SeatLockFailedException("Seat locking failed: " + errorMsg);
		}

		log.info("Seats locked successfully: {}", response.getData().getLockedSeats());
		return response.getData();
	}

	@Override
	@CircuitBreaker(name = "seatService", fallbackMethod = "confirmSeatsFallback")
	@Retry(name = "seatService")
	public void confirmSeats(ConfirmSeatsRequestDto request) {
		log.info("Confirming seats via Feign: showId={}, seats={}",
				request.getShowId(), request.getSeatNumbers());

		ApiResponseDto<Void> response = showSeatFeignClient.confirmSeats(request);

		if (response != null && !Boolean.TRUE.equals(response.getSuccess())) {
			log.error("Seat confirmation failed: {}", response.getError());
			throw new RuntimeException("Seat confirmation failed: " + response.getError());
		}

		log.info("Seats confirmed successfully for show: {}", request.getShowId());
	}

	@Override
	@CircuitBreaker(name = "seatService", fallbackMethod = "unlockSeatsFallback")
	@Retry(name = "seatService")
	public void unlockSeats(UnlockSeatsRequestDto request) {
		log.info("Unlocking seats via Feign: showId={}, seats={}",
				request.getShowId(), request.getSeatNumbers());

		ApiResponseDto<Void> response = showSeatFeignClient.unlockSeats(request);

		if (response != null) {
			log.info("Seats unlocked: {}", response.getMessage());
		}
	}

	// Fallback methods for circuit breaker
	private LockSeatsResponseDto lockSeatsFallback(LockSeatsRequestDto request, Exception ex) {
		log.error("Circuit breaker: Seat service unavailable for lock. Error: {}", ex.getMessage());
		throw new SeatLockFailedException("Seat service temporarily unavailable. Please try again later.");
	}

	private void confirmSeatsFallback(ConfirmSeatsRequestDto request, Exception ex) {
		log.error("Circuit breaker: Seat service unavailable for confirm. Error: {}", ex.getMessage());
		// In production, this should be queued for retry
		throw new RuntimeException("Failed to confirm seats - service unavailable");
	}

	private void unlockSeatsFallback(UnlockSeatsRequestDto request, Exception ex) {
		log.error("Circuit breaker: Seat service unavailable for unlock. Error: {}", ex.getMessage());
		// Seat locks will expire automatically via TTL, so this is less critical
	}
}