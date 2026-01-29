package com.quicktix.showseat_service.model.redis;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBooking implements Serializable {
    
    private static final long serialVersionUID = -6172560596762332414L;

	/**
     * Show ID
     */
    private String showId;
    
    /**
     * Seat number
     */
    private String seatNo;
    
    /**
     * User ID who booked the seat
     */
    private Long userId;
    
    /**
     * Booking ID from Booking Service
     */
    private Long bookingId;
    
    /**
     * Booking timestamp
     */
    private LocalDateTime bookedAt;
}
