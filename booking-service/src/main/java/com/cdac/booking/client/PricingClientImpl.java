package com.cdac.booking.client;

import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.dto.seat.GetSeatPricesRequest;
import com.cdac.booking.feign.ShowSeatFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pricing client implementation using OpenFeign with Eureka service discovery.
 * 
 * Fetches dynamic pricing from ShowSeat service and falls back to
 * default pricing when the service is unavailable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PricingClientImpl implements PricingClient {

    private final ShowSeatFeignClient showSeatFeignClient;

    @Value("${quicktix.pricing.default-seat-price:150}")
    private BigDecimal defaultSeatPrice;

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getSeatPricesFallback")
    @Retry(name = "pricingService")
    public Map<String, BigDecimal> getSeatPrices(String showId, List<String> seatNumbers) {
        log.info("Fetching seat prices via Feign for show {}: {}", showId, seatNumbers);

        GetSeatPricesRequest request = GetSeatPricesRequest.builder()
                .seatNumbers(seatNumbers)
                .build();

        ApiResponseDto<Map<String, BigDecimal>> response = showSeatFeignClient.getSeatPrices(showId, request);

        if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getData() != null) {
            log.info("Retrieved prices for {} seats", response.getData().size());
            return response.getData();
        }

        log.warn("Invalid pricing response, using defaults");
        return getDefaultPrices(seatNumbers);
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getShowBasePriceFallback")
    @Retry(name = "pricingService")
    public BigDecimal getShowBasePrice(String showId) {
        log.info("Fetching base price via Feign for show: {}", showId);

        ApiResponseDto<Map<String, Object>> response = showSeatFeignClient.getShowBasePrice(showId);

        if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getData() != null) {
            Object price = response.getData().get("basePrice");
            if (price != null) {
                BigDecimal basePrice = new BigDecimal(price.toString());
                log.info("Retrieved base price: {}", basePrice);
                return basePrice;
            }
        }

        log.warn("Invalid base price response, using default");
        return defaultSeatPrice;
    }

    /**
     * Fallback when pricing service is unavailable - use default pricing
     */
    private Map<String, BigDecimal> getSeatPricesFallback(String showId, List<String> seatNumbers, Exception ex) {
        log.warn("Using fallback pricing for show {} due to: {}", showId, ex.getMessage());
        return getDefaultPrices(seatNumbers);
    }

    /**
     * Fallback for base price
     */
    private BigDecimal getShowBasePriceFallback(String showId, Exception ex) {
        log.warn("Using fallback base price for show {} due to: {}", showId, ex.getMessage());
        return defaultSeatPrice;
    }

    /**
     * Generate default prices for all seats
     */
    private Map<String, BigDecimal> getDefaultPrices(List<String> seatNumbers) {
        Map<String, BigDecimal> prices = new HashMap<>();
        for (String seat : seatNumbers) {
            prices.put(seat, defaultSeatPrice);
        }
        return prices;
    }
}
