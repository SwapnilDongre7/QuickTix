package com.quicktix.showseat_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsResponse {
    
    private String showId;
    private List<String> lockedSeats;
    private List<String> failedSeats;
    private LocalDateTime lockedAt;
    private LocalDateTime expiresAt;
    private Integer lockDurationSeconds;
    private String sessionId;
}