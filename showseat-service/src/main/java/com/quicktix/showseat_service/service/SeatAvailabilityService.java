package com.quicktix.showseat_service.service;

import com.quicktix.showseat_service.dto.response.SeatAvailabilityResponse;

public interface SeatAvailabilityService {

    SeatAvailabilityResponse getSeatAvailability(String showId);

    boolean isSeatAvailable(String showId, String seatNo);

    /**
     * Returns a Base64 encoded bitmask of seat statuses.
     */
    String getCompressedAvailability(String showId);
}