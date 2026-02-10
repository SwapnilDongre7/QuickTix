package com.quicktix.showseat_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.quicktix.showseat_service.config.AppConfig;
import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
import com.quicktix.showseat_service.dto.response.LockSeatsResponse;
import com.quicktix.showseat_service.exception.LayoutNotFoundException;
import com.quicktix.showseat_service.exception.SeatAlreadyBookedException;
import com.quicktix.showseat_service.exception.SeatNotLockedException;
import com.quicktix.showseat_service.exception.ShowNotFoundException;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;
import com.quicktix.showseat_service.repository.ShowRepository;
import com.quicktix.showseat_service.util.RedisBitmapUtil;
import com.quicktix.showseat_service.validator.SeatValidator;
import com.quicktix.showseat_service.validator.ShowValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements SeatLockService {

    private final ShowRepository showRepository;
    private final SeatLayoutRepository layoutRepository;
    private final RedisBitmapUtil redisBitmapUtil;
    private final SeatValidator seatValidator;
    private final ShowValidator showValidator;
    private final ShowService showService;
    private final AppConfig appConfig;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));

        showValidator.validateBookingAllowed(show);

        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + show.getLayoutId()));

        seatValidator.validateNoDuplicateSeats(request.getSeatNumbers());
        seatValidator.validateSeatCount(
                request.getSeatNumbers(),
                appConfig.getSeatLock().getMaxSeatsPerBooking());
        seatValidator.validateSeatNumbers(layout, request.getSeatNumbers());

        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (seatIndices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        // Use atomic locking - this will throw SeatAlreadyLockedException or
        // SeatAlreadyBookedException
        // if any seat cannot be locked
        List<Integer> lockedIndices = redisBitmapUtil.lockSeatsAtomic(
                request.getShowId(),
                seatIndices,
                request.getUserId(),
                request.getSessionId(),
                appConfig.getSeatLock().getTtlSeconds());

        List<String> lockedSeats = convertToSeatNumbers(request.getShowId(), lockedIndices);

        LocalDateTime now = LocalDateTime.now();

        log.info("Lock seats result - Success: {}", lockedSeats.size());

        broadcastUpdate(request.getShowId(), lockedSeats, "LOCKED", request.getUserId());

        return LockSeatsResponse.builder()
                .showId(request.getShowId())
                .lockedSeats(lockedSeats)
                .failedSeats(new ArrayList<>()) // With atomic locking, either all succeed or exception is thrown
                .lockedAt(now)
                .expiresAt(now.plusSeconds(appConfig.getSeatLock().getTtlSeconds()))
                .lockDurationSeconds(appConfig.getSeatLock().getTtlSeconds())
                .sessionId(request.getSessionId())
                .build();
    }

    @Override
    public void unlockSeats(UnlockSeatsRequest request) {
        showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));

        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (indices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        int unlockedCount = redisBitmapUtil.unlockSeats(
                request.getShowId(),
                indices,
                request.getUserId(),
                request.getSessionId());

        if (unlockedCount == 0) {
            throw new SeatNotLockedException(
                    "No seats were unlocked. Seats are not locked by this user/session.");
        }

        log.info("Unlocked {} seats for show {}", unlockedCount, request.getShowId());

        // Broadcast update
        broadcastUpdate(request.getShowId(), request.getSeatNumbers(), "AVAILABLE", request.getUserId());
    }

    @Override
    public void confirmSeats(ConfirmSeatsRequest request) {
        String showId = request.getShowId();
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));

        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (indices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        // Idempotency check: If booking is already processed, return success
        // immediately
        if (redisBitmapUtil.isBookingProcessed(String.valueOf(request.getBookingId()))) {
            log.info("Booking {} already processed (idempotent retry). Returning success.", request.getBookingId());
            return;
        }

        // Strict validation causes issues if payment takes longer than TTL.
        // We relax validation: Allow confirm if (Locked by Us) OR (Expired AND
        // Available)

        for (Integer index : indices) {
            boolean isLockedByUs = redisBitmapUtil.isSeatLockedByUser(showId, index, request.getUserId(),
                    request.getSessionId());

            if (isLockedByUs) {
                continue; // Safe to confirm
            }

            // If not locked by us, check if it's taken by someone else
            if (redisBitmapUtil.isSeatBooked(showId, index)) {
                String seatNo = redisBitmapUtil.getSeatNumber(showId, index);
                throw new SeatAlreadyBookedException(
                        "Seat " + (seatNo != null ? seatNo : index) + " is already booked");
            }

            if (redisBitmapUtil.isSeatLocked(showId, index)) {
                String seatNo = redisBitmapUtil.getSeatNumber(showId, index);
                throw new SeatNotLockedException(
                        "Seat " + (seatNo != null ? seatNo : index) + " is locked by another user");
            }

            // If neither booked nor locked by others, it implies our lock expired but seat
            // is safe to take.
            log.info("Seat {} lock expired but still available. Allowing confirmation for user {}", index,
                    request.getUserId());
        }

        for (Integer index : indices) {
            if (redisBitmapUtil.isSeatBooked(request.getShowId(), index)) {
                String seatNo = redisBitmapUtil.getSeatNumber(request.getShowId(), index);
                throw new SeatAlreadyBookedException("Seat " + seatNo + " is already booked");
            }
        }

        redisBitmapUtil.confirmBooking(
                request.getShowId(),
                indices,
                request.getUserId(),
                request.getSessionId());

        showService.updateAvailableSeats(
                request.getShowId(),
                request.getSeatNumbers().size());

        log.info("Confirm seats completed for show {}, booking {}", request.getShowId(), request.getBookingId());

        broadcastUpdate(request.getShowId(), request.getSeatNumbers(), "BOOKED", request.getUserId());

        // Mark booking as processed to ensure idempotency
        redisBitmapUtil.markBookingProcessed(String.valueOf(request.getBookingId()));
    }

    @Override
    public void expireLocks(String showId) {
        redisBitmapUtil.clearLocksOnly(showId);
        log.info("Expired all locks for show {}", showId);
        // We might want to broadcast a full refresh or just expire event
        // For now, simpler to not broadcast blindly or clients can't update without
        // list
        // Ideally we fetch all locked seats before expiring, but that's heavy.
        // Client side timeouts or refresh on error handle this usually.
    }

    private void broadcastUpdate(String showId, List<String> seats, String status, Long userId) {
        if (seats == null || seats.isEmpty())
            return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("showId", showId);
        payload.put("seats", seats);
        payload.put("status", status);
        payload.put("userId", userId);
        payload.put("timestamp", System.currentTimeMillis());

        try {
            messagingTemplate.convertAndSend("/topic/show/" + showId, payload);
            log.debug("Broadcasted {} update for seats: {}", status, seats);
        } catch (Exception e) {
            log.error("Failed to broadcast seat update", e);
        }
    }

    private List<Integer> convertToIndices(String showId, List<String> seatNumbers) {
        List<Integer> indices = new ArrayList<>();
        for (String seat : seatNumbers) {
            Integer index = redisBitmapUtil.getSeatIndex(showId, seat);
            if (index != null) {
                indices.add(index);
            } else {
                log.warn("Seat {} not found in mapping for show {}", seat, showId);
            }
        }
        return indices;
    }

    private List<String> convertToSeatNumbers(String showId, List<Integer> indices) {
        List<String> seats = new ArrayList<>();
        for (Integer index : indices) {
            String seat = redisBitmapUtil.getSeatNumber(showId, index);
            if (seat != null) {
                seats.add(seat);
            } else {
                log.warn("Index {} not found in mapping for show {}", index, showId);
            }
        }
        return seats;
    }
}