package com.cdac.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CreateBookingResponse {

	private Long bookingId;
	private String bookingStatus;
	private String paymentStatus;
	private BigDecimal amount;
}