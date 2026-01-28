package com.cdac.booking.dto.seat;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class LockSeatsResponseDto {
	private String showId;
	private List<String> lockedSeats;
	private List<String> failedSeats;
	private LocalDateTime lockedAt;
	private LocalDateTime expiresAt;
	private Integer lockDurationSeconds;
	private String sessionId;
}