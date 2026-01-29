package com.cdac.booking.feign.fallback;

import com.cdac.booking.dto.seat.*;
import com.cdac.booking.exception.SeatLockFailedException;
import com.cdac.booking.feign.ShowSeatFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback factory for ShowSeatFeignClient.
 * 
 * Provides graceful degradation when ShowSeat service is unavailable.
 * - Seat locking fails with SeatLockFailedException
 * - Pricing returns default fallback values
 */
@Slf4j
@Component
public class ShowSeatFeignClientFallbackFactory implements FallbackFactory<ShowSeatFeignClient> {

    @Override
    public ShowSeatFeignClient create(Throwable cause) {
        log.error("ShowSeat service fallback triggered: {}", cause.getMessage());

        return new ShowSeatFeignClient() {

            @Override
            public ApiResponseDto<LockSeatsResponseDto> lockSeats(LockSeatsRequestDto request) {
                log.error("Fallback: Unable to lock seats for show {}. Error: {}",
                        request.getShowId(), cause.getMessage());
                throw new SeatLockFailedException(
                        "Seat service temporarily unavailable. Please try again later.");
            }

            @Override
            public ApiResponseDto<Void> confirmSeats(ConfirmSeatsRequestDto request) {
                log.error("Fallback: Unable to confirm seats for show {}. Error: {}",
                        request.getShowId(), cause.getMessage());
                // In production, this should be queued for retry
                throw new RuntimeException(
                        "Failed to confirm seats - service unavailable. Booking may need manual confirmation.");
            }

            @Override
            public ApiResponseDto<Void> unlockSeats(UnlockSeatsRequestDto request) {
                log.warn("Fallback: Unable to unlock seats for show {}. Error: {}. " +
                        "Seats will expire automatically via TTL.",
                        request.getShowId(), cause.getMessage());
                // Seat locks will expire automatically via TTL, so this is less critical
                ApiResponseDto<Void> response = new ApiResponseDto<>();
                response.setSuccess(true);
                response.setMessage("Unlock queued - seats will expire automatically");
                return response;
            }

            @Override
            public ApiResponseDto<Map<String, BigDecimal>> getSeatPrices(
                    String showId, GetSeatPricesRequest request) {
                log.warn("Fallback: Using default pricing for show {}. Error: {}",
                        showId, cause.getMessage());

                // Return default pricing as fallback
                Map<String, BigDecimal> defaultPrices = new HashMap<>();
                BigDecimal defaultPrice = BigDecimal.valueOf(150);
                for (String seat : request.getSeatNumbers()) {
                    defaultPrices.put(seat, defaultPrice);
                }

                ApiResponseDto<Map<String, BigDecimal>> response = new ApiResponseDto<>();
                response.setSuccess(true);
                response.setData(defaultPrices);
                response.setMessage("Fallback pricing applied");
                return response;
            }

            @Override
            public ApiResponseDto<Map<String, Object>> getShowBasePrice(String showId) {
                log.warn("Fallback: Using default base price for show {}. Error: {}",
                        showId, cause.getMessage());

                Map<String, Object> response = new HashMap<>();
                response.put("showId", showId);
                response.put("basePrice", BigDecimal.valueOf(150));

                ApiResponseDto<Map<String, Object>> apiResponse = new ApiResponseDto<>();
                apiResponse.setSuccess(true);
                apiResponse.setData(response);
                apiResponse.setMessage("Fallback pricing applied");
                return apiResponse;
            }
        };
    }
}
