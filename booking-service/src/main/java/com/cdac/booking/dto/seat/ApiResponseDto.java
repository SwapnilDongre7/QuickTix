package com.cdac.booking.dto.seat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ApiResponseDto<T> {
	private Boolean success;
	private String message;
	private T data;
	private String error;
}