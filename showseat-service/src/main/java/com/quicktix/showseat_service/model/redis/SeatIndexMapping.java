package com.quicktix.showseat_service.model.redis;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatIndexMapping implements Serializable {
    
    private static final long serialVersionUID = -5617577145434156723L;

	/**
     * Show ID
     */
    private String showId;
    
    /**
     * Map of seat number to bitmap index
     * Example: {"A1": 0, "A2": 1, "B1": 2, ...}
     */
    private Map<String, Integer> seatToIndex;
    
    /**
     * Reverse map: bitmap index to seat number
     * Example: {0: "A1", 1: "A2", 2: "B1", ...}
     */
    private Map<Integer, String> indexToSeat;
}