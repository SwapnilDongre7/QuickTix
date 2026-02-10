package com.cdac.booking.feign;

import com.cdac.booking.dto.seat.*;
import com.cdac.booking.feign.config.FeignConfig;
import com.cdac.booking.feign.dto.ShowResponseDto;
import com.cdac.booking.feign.fallback.ShowSeatFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for ShowSeat Service.
 * 
 * Provides declarative REST client for all seat-related operations:
 * - Seat locking/unlocking
 * - Seat confirmation
 * - Pricing queries
 * 
 * Uses Eureka service discovery via service name "SHOWSEAT-SERVICE".
 */
@FeignClient(name = "SHOWSEAT-SERVICE", configuration = FeignConfig.class, fallbackFactory = ShowSeatFeignClientFallbackFactory.class)
public interface ShowSeatFeignClient {

    /**
     * Lock seats for a user during the booking process.
     */
    @PostMapping("/seats/lock")
    ApiResponseDto<LockSeatsResponseDto> lockSeats(@RequestBody LockSeatsRequestDto request);

    /**
     * Confirm seats after successful payment.
     */
    @PostMapping("/seats/confirm")
    ApiResponseDto<Void> confirmSeats(@RequestBody ConfirmSeatsRequestDto request);

    /**
     * Unlock seats (user cancellation or timeout).
     */
    @PostMapping("/seats/unlock")
    ApiResponseDto<Void> unlockSeats(@RequestBody UnlockSeatsRequestDto request);

    /**
     * Get prices for specific seats in a show.
     */
    @PostMapping("/api/show-seat/pricing/{showId}")
    ApiResponseDto<Map<String, BigDecimal>> getSeatPrices(
            @PathVariable("showId") String showId,
            @RequestBody GetSeatPricesRequest request);

    /**
     * Get base price for a show.
     */
    @GetMapping("/api/show-seat/pricing/shows/{showId}/price")
    ApiResponseDto<Map<String, Object>> getShowBasePrice(@PathVariable("showId") String showId);

    /**
     * Get show details by ID.
     * Used for enriching ticket response with show information.
     */
    @GetMapping("/api/shows/{showId}")
    ApiResponseDto<ShowResponseDto> getShowById(@PathVariable("showId") String showId);
}
