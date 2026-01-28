package com.quicktix.showseat_service.model.document;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pricing {
    
    /**
     * Price for SILVER seats
     */
    private BigDecimal silver;
    
    /**
     * Price for GOLD seats
     */
    private BigDecimal gold;
    
    /**
     * Price for PLATINUM seats
     */
    private BigDecimal platinum;
    
    /**
     * Price for DIAMOND seats
     */
    private BigDecimal diamond;
    
    /**
     * Get price for a specific seat type
     */
    public BigDecimal getPriceForSeatType(String seatType) {
        return switch (seatType.toUpperCase()) {
            case "SILVER" -> silver;
            case "GOLD" -> gold;
            case "PLATINUM" -> platinum;
            case "DIAMOND" -> diamond;
            default -> BigDecimal.ZERO;
        };
    }
}