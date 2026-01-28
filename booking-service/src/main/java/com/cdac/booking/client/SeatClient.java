package com.cdac.booking.client;

import com.cdac.booking.dto.seat.*;

public interface SeatClient {

	LockSeatsResponseDto lockSeats(LockSeatsRequestDto request);

	void confirmSeats(ConfirmSeatsRequestDto request);

	void unlockSeats(UnlockSeatsRequestDto request);
}