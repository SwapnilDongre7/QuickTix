package com.quicktix.showseat_service.controller;

import com.quicktix.showseat_service.dto.request.GetSeatPricesRequest;
import com.quicktix.showseat_service.dto.response.ApiResponse;
import com.quicktix.showseat_service.exception.ShowNotFoundException;
import com.quicktix.showseat_service.model.document.Pricing;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;
import com.quicktix.showseat_service.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Pricing Controller for seat pricing operations.
 * 
 * Provides endpoints for booking-service to fetch dynamic
 * seat prices based on seat type and show configuration.
 */
@Slf4j
@RestController
@RequestMapping("/api/show-seat/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final ShowRepository showRepository;
    private final SeatLayoutRepository layoutRepository;

    /**
     * Get prices for specific seats in a show.
     * 
     * Returns a map of seat number to price based on seat type.
     * Seat types (SILVER, GOLD, PLATINUM, DIAMOND) are determined
     * from the seat layout configuration.
     */
    @PostMapping("/{showId}")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getSeatPrices(
            @PathVariable String showId,
            @RequestBody GetSeatPricesRequest request) {
        log.info("Getting prices for show {} seats: {}", showId, request.getSeatNumbers());

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));

        Pricing pricing = show.getPricing();
        if (pricing == null) {
            log.warn("Show {} has no pricing configured, using defaults", showId);
            pricing = Pricing.builder()
                    .silver(BigDecimal.valueOf(150))
                    .gold(BigDecimal.valueOf(250))
                    .platinum(BigDecimal.valueOf(350))
                    .diamond(BigDecimal.valueOf(500))
                    .build();
        }

        // Get seat layout to determine seat types
        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
                .orElse(null);

        Map<String, BigDecimal> seatPrices = new HashMap<>();

        for (String seatNumber : request.getSeatNumbers()) {
            String seatType = getSeatTypeFromLayout(layout, seatNumber);
            BigDecimal price = pricing.getPriceForSeatType(seatType);
            seatPrices.put(seatNumber, price);
        }

        log.info("Calculated prices for {} seats", seatPrices.size());

        return ResponseEntity.ok(ApiResponse.success(seatPrices, "Seat prices retrieved"));
    }

    /**
     * Get base price for a show (lowest tier price).
     */
    @GetMapping("/shows/{showId}/price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getShowBasePrice(
            @PathVariable String showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));

        Pricing pricing = show.getPricing();
        BigDecimal basePrice = BigDecimal.valueOf(150); // Default

        if (pricing != null && pricing.getSilver() != null) {
            basePrice = pricing.getSilver();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("showId", showId);
        response.put("basePrice", basePrice);
        response.put("pricing", pricing);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Determine seat type from layout based on seat number.
     * 
     * This is a simplified implementation - in production,
     * seat types would be stored in the layout configuration.
     */
    private String getSeatTypeFromLayout(SeatLayout layout, String seatNumber) {
        // Parse row from seat number (e.g., "A1" -> "A")
        if (seatNumber == null || seatNumber.isEmpty()) {
            return "SILVER";
        }

        char rowChar = seatNumber.charAt(0);

        // Simple row-based classification
        // Rows A-C: DIAMOND (front premium)
        // Rows D-F: PLATINUM
        // Rows G-J: GOLD
        // Rows K+: SILVER

        if (rowChar >= 'A' && rowChar <= 'C') {
            return "DIAMOND";
        } else if (rowChar >= 'D' && rowChar <= 'F') {
            return "PLATINUM";
        } else if (rowChar >= 'G' && rowChar <= 'J') {
            return "GOLD";
        } else {
            return "SILVER";
        }
    }
}
