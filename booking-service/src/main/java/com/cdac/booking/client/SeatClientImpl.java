package com.cdac.booking.client;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.dto.seat.ConfirmSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsResponseDto;
import com.cdac.booking.dto.seat.UnlockSeatsRequestDto;
import com.cdac.booking.exception.SeatLockFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Profile("!local-mock-seat")
public class SeatClientImpl implements SeatClient {

	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public LockSeatsResponseDto lockSeats(LockSeatsRequestDto request) {

		String url = "http://localhost:8082/seats/lock";

		ResponseEntity<ApiResponseDto> response = restTemplate.postForEntity(url, request, ApiResponseDto.class);

		ApiResponseDto<?> apiResponse = response.getBody();

		if (apiResponse == null || !Boolean.TRUE.equals(apiResponse.getSuccess())) {
			throw new SeatLockFailedException("Seat locking failed");
		}

		return new ObjectMapper().convertValue(apiResponse.getData(), LockSeatsResponseDto.class);
	}

	@Override
	public void confirmSeats(ConfirmSeatsRequestDto request) {
		restTemplate.postForEntity("http://localhost:8082/seats/confirm", request, Void.class);
	}

	@Override
	public void unlockSeats(UnlockSeatsRequestDto request) {
		restTemplate.postForEntity("http://localhost:8082/seats/unlock", request, Void.class);
	}
}