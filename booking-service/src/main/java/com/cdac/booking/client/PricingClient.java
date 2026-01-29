package com.cdac.booking.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Client interface for fetching seat pricing from ShowSeat service
 */
public interface PricingClient {

    /**
     * Get pricing for seats in a show
     * 
     * @param showId      Show ID
     * @param seatNumbers List of seat numbers
     * @return Map of seat number to price
     */
    Map<String, BigDecimal> getSeatPrices(String showId, List<String> seatNumbers);

    /**
     * Get base price for a show
     * 
     * @param showId Show ID
     * @return Base price for the show
     */
    BigDecimal getShowBasePrice(String showId);
}
