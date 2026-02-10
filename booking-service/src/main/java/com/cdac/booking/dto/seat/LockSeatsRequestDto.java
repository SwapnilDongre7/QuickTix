package com.cdac.booking.dto.seat;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class LockSeatsRequestDto {
	private String showId;
	private Long userId;
	private String sessionId;
	private List<String> seatNumbers;
}