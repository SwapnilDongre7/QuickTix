package com.cdac.booking.scheduler;

import com.cdac.booking.client.SeatClient;
import com.cdac.booking.dto.seat.UnlockSeatsRequestDto;
import com.cdac.booking.entity.Booking;
import com.cdac.booking.entity.BookingStatus;
import com.cdac.booking.entity.PaymentStatus;
import com.cdac.booking.repository.BookingRepository;
import com.cdac.booking.repository.BookingSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduler to cleanup abandoned bookings.
 * 
 * This scheduler runs periodically to:
 * 1. Find INITIATED bookings older than the configured timeout
 * 2. Unlock the seats that were locked for these bookings
 * 3. Mark the bookings as EXPIRED
 * 
 * This prevents database pollution and ensures seats are released
 * when users abandon the booking flow without completing payment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatClient seatClient;

    @Value("${quicktix.booking.cleanup.timeout-minutes:5}")
    private int cleanupTimeoutMinutes;

    /**
     * Cleanup abandoned bookings every minute.
     * 
     * Bookings in INITIATED status for more than 5 minutes (configurable)
     * are considered abandoned and will be expired.
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void cleanupAbandonedBookings() {
        log.debug("Running booking cleanup job...");

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cleanupTimeoutMinutes);

        List<Booking> abandonedBookings = bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.INITIATED, cutoff);

        if (abandonedBookings.isEmpty()) {
            log.debug("No abandoned bookings found");
            return;
        }

        log.info("Found {} abandoned bookings to cleanup", abandonedBookings.size());

        int successCount = 0;
        int failCount = 0;

        for (Booking booking : abandonedBookings) {
            try {
                expireBooking(booking);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("Failed to cleanup booking {}: {}", booking.getId(), e.getMessage());
            }
        }

        log.info("Booking cleanup completed: {} expired, {} failed", successCount, failCount);
    }

    /**
     * Expire a single booking and unlock its seats
     */
    private void expireBooking(Booking booking) {
        log.info("Expiring abandoned booking: id={}, userId={}, showId={}",
                booking.getId(), booking.getUserId(), booking.getShowId());

        // Get the seat numbers for this booking
        List<String> seatNumbers = bookingSeatRepository.findByBookingId(booking.getId())
                .stream()
                .map(seat -> seat.getSeatNo())
                .collect(Collectors.toList());

        // Unlock the seats if not already unlocked
        if (!booking.isSeatsUnlocked() && !seatNumbers.isEmpty()) {
            try {
                UnlockSeatsRequestDto unlockRequest = UnlockSeatsRequestDto.builder()
                        .showId(booking.getShowId())
                        .userId(booking.getUserId())
                        .sessionId(booking.getSeatSessionId())
                        .seatNumbers(seatNumbers)
                        .build();

                seatClient.unlockSeats(unlockRequest);
                booking.setSeatsUnlocked(true);
                log.info("Unlocked {} seats for booking {}", seatNumbers.size(), booking.getId());
            } catch (Exception e) {
                // Log but don't fail - seats will expire via TTL anyway
                log.warn("Failed to unlock seats for booking {}: {}", booking.getId(), e.getMessage());
            }
        }

        // Update booking status
        try {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
            // Use saveAndFlush to force DB error to occur HERE, inside try-catch
            bookingRepository.saveAndFlush(booking);
            log.info("Booking {} marked as EXPIRED", booking.getId());
        } catch (Exception e) {
            log.warn("Failed to mark booking {} as EXPIRED (likely due to DB schema mismatch). Fallback to CANCELLED.",
                    booking.getId());
            // Fallback for when the DB enum doesn't support EXPIRED yet
            try {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setPaymentStatus(PaymentStatus.FAILED);
                bookingRepository.saveAndFlush(booking);
                log.info("Booking {} marked as CANCELLED (fallback)", booking.getId());
            } catch (Exception ex) {
                log.error("Failed to mark booking {} as CANCELLED during fallback: {}", booking.getId(),
                        ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * Get count of currently abandoned bookings (for monitoring)
     */
    public long getAbandonedBookingCount() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cleanupTimeoutMinutes);
        return bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.INITIATED, cutoff)
                .size();
    }
}
