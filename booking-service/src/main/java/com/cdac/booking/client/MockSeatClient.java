package com.cdac.booking.client;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.cdac.booking.dto.seat.ConfirmSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsResponseDto;
import com.cdac.booking.dto.seat.UnlockSeatsRequestDto;

@Component
@Profile("local-mock-seat")
public class MockSeatClient implements SeatClient {

    @Override
    public LockSeatsResponseDto lockSeats(LockSeatsRequestDto request) {

        LockSeatsResponseDto response = new LockSeatsResponseDto();
        response.setShowId(request.getShowId());
        response.setLockedSeats(request.getSeatNumbers());
        response.setFailedSeats(List.of()); // ✅ IMPORTANT
        response.setSessionId(request.getSessionId());
        response.setLockedAt(LocalDateTime.now());
        response.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        response.setLockDurationSeconds(300);

        return response;
    }

    @Override
    public void confirmSeats(ConfirmSeatsRequestDto request) {
        // no-op
    }

    @Override
    public void unlockSeats(UnlockSeatsRequestDto request) {
        // no-op
    }
}