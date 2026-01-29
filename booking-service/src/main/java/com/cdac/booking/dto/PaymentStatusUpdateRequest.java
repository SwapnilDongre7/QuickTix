package com.cdac.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentStatusUpdateRequest {

	private Long bookingId;
	private String paymentStatus; // SUCCESS / FAILED
}