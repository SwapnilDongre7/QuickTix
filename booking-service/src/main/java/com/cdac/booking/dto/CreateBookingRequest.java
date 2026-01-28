package com.cdac.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBookingRequest {

	@NotNull
	private Long userId;

	@NotNull
	private String showId;

	@NotEmpty
	private List<String> seatNos;
}