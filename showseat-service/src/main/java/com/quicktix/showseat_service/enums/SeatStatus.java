package com.quicktix.showseat_service.enums;

public enum SeatStatus {
    /**
     * Seat is available for booking
     */
    AVAILABLE,
    
    /**
     * Seat is temporarily locked by a user (during booking process)
     */
    LOCKED,
    
    /**
     * Seat is permanently booked
     */
    BOOKED,
    
    /**
     * Seat is blocked by admin/theatre owner
     */
    BLOCKED
}
