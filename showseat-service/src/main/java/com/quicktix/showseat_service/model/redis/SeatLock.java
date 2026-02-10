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
public class SeatLock implements Serializable {
    
    private static final long serialVersionUID = 4633551206681470559L;

	/**
     * Show ID
     */
    private String showId;
    
    /**
     * Seat index in bitmap (not seat number)
     */
    private Integer seatIndex;
    
    /**
     * User ID who locked the seat
     */
    private Long userId;
    
    /**
     * Lock creation timestamp
     */
    private LocalDateTime lockedAt;
    
    /**
     * Lock expiry timestamp (TTL in Redis)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Session/Booking ID for tracking
     */
    private String sessionId;
}