package com.quicktix.showseat_service.dto.response;

import java.util.Map;

import com.quicktix.showseat_service.enums.SeatStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    
    private String showId;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer bookedSeats;
    private Integer lockedSeats;
    
    /**
     * Map of seat number to its current status
     * Example: {"A1": "AVAILABLE", "A2": "BOOKED", "A3": "LOCKED"}
     */
    private Map<String, SeatStatus> seatStatusMap;
}