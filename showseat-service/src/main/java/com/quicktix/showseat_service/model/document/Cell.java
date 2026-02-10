package com.quicktix.showseat_service.model.document;

import com.quicktix.showseat_service.enums.CellType;
import com.quicktix.showseat_service.enums.SeatType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    
    /**
     * Column index in the row (0-based)
     */
    private Integer col;
    
    /**
     * Type of cell (SEAT, SPACE, WHEELCHAIR, PREMIUM)
     */
    private CellType type;
    
    /**
     * Seat number (e.g., "A1", "B5") - null for SPACE type
     */
    private String seatNo;
    
    /**
     * Seat category for pricing - null for SPACE type
     */
    private SeatType seatType;
    
    /**
     * Display label for UI (optional)
     */
    private String label;
}