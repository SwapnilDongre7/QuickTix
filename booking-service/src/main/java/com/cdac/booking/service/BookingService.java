package com.cdac.booking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.booking.client.PaymentClient;
import com.cdac.booking.client.PricingClient;
import com.cdac.booking.client.SeatClient;
import com.cdac.booking.dto.CreateBookingRequest;
import com.cdac.booking.dto.CreateBookingResponse;
import com.cdac.booking.dto.seat.ConfirmSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsResponseDto;
import com.cdac.booking.dto.seat.UnlockSeatsRequestDto;
import com.cdac.booking.entity.Booking;
import com.cdac.booking.entity.BookingSeat;
import com.cdac.booking.entity.BookingStatus;
import com.cdac.booking.entity.PaymentStatus;
import com.cdac.booking.exception.SeatLockFailedException;
import com.cdac.booking.repository.BookingRepository;
import com.cdac.booking.repository.BookingSeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Booking service with dynamic pricing support.
 * 
 * Prices are fetched from ShowSeat service via PricingClient.
 * Falls back to default pricing if pricing service is unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository bookingRepository;
	private final BookingSeatRepository bookingSeatRepository;
	private final PaymentClient paymentClient;
	private final SeatClient seatClient;
	private final PricingClient pricingClient;

	// =========================
	// CREATE BOOKING
	// =========================
	@Transactional
	public CreateBookingResponse createBooking(String idempotencyKey, CreateBookingRequest request) {

		return bookingRepository.findByIdempotencyKey(idempotencyKey)
				.map(existing -> CreateBookingResponse.builder().bookingId(existing.getId())
						.bookingStatus(existing.getStatus().name()).paymentStatus(existing.getPaymentStatus().name())
						.amount(existing.getTotalAmount()).build())
				.orElseGet(() -> createNewBooking(idempotencyKey, request));
	}

	private CreateBookingResponse createNewBooking(String idempotencyKey, CreateBookingRequest request) {

		String sessionId = UUID.randomUUID().toString();

		LockSeatsRequestDto lockRequest = LockSeatsRequestDto.builder().showId(request.getShowId())
				.userId(request.getUserId()).sessionId(sessionId).seatNumbers(request.getSeatNos()).build();

		LockSeatsResponseDto lockResponse = seatClient.lockSeats(lockRequest);

		// CRITICAL SAFETY CHECK
		List<String> failedSeats = lockResponse.getFailedSeats();
		if (failedSeats != null && !failedSeats.isEmpty()) {
			throw new SeatLockFailedException("Seats could not be locked: " + failedSeats);
		}

		List<String> confirmedSeats = lockResponse.getLockedSeats();

		// ========================================
		// DYNAMIC PRICING - Fetch from ShowSeat
		// ========================================
		Map<String, BigDecimal> seatPrices = pricingClient.getSeatPrices(
				request.getShowId(),
				confirmedSeats);

		log.info("Fetched dynamic pricing for {} seats: {}", confirmedSeats.size(), seatPrices);

		// Calculate total amount from dynamic prices
		BigDecimal totalAmount = seatPrices.values().stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Booking booking = bookingRepository
				.save(Booking.builder().userId(request.getUserId()).showId(request.getShowId()).seatSessionId(sessionId)
						.totalAmount(totalAmount).status(BookingStatus.INITIATED).paymentStatus(PaymentStatus.PENDING)
						.idempotencyKey(idempotencyKey).build());

		// Save individual seat prices
		confirmedSeats.forEach(seat -> {
			BigDecimal seatPrice = seatPrices.getOrDefault(seat, BigDecimal.ZERO);
			bookingSeatRepository.save(
					BookingSeat.builder()
							.bookingId(booking.getId())
							.seatNo(seat)
							.price(seatPrice)
							.build());
		});

		paymentClient.initiatePayment(booking.getId(), booking.getUserId(), booking.getTotalAmount(), idempotencyKey);

		return CreateBookingResponse.builder().bookingId(booking.getId()).bookingStatus(booking.getStatus().name())
				.paymentStatus(booking.getPaymentStatus().name()).amount(booking.getTotalAmount()).build();
	}

	// =========================
	// PAYMENT CALLBACK (RETRY SAFE)
	// =========================
	@Transactional
	public void handlePaymentUpdate(Long bookingId, String paymentStatus) {

		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

		// 🔁 Guard against duplicate callbacks
		if (booking.getPaymentStatus().name().equalsIgnoreCase(paymentStatus)) {
			return;
		}

		List<String> seatNumbers = bookingSeatRepository.findByBookingId(bookingId).stream().map(BookingSeat::getSeatNo)
				.collect(Collectors.toList());

		if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

			booking.setPaymentStatus(PaymentStatus.SUCCESS);
			booking.setStatus(BookingStatus.CONFIRMED);

			if (!booking.isSeatsConfirmed()) {
				seatClient.confirmSeats(
						ConfirmSeatsRequestDto.builder().showId(booking.getShowId()).userId(booking.getUserId())
								.sessionId(booking.getSeatSessionId()).seatNumbers(seatNumbers).build());
				booking.setSeatsConfirmed(true);
			}

		} else {

			booking.setPaymentStatus(PaymentStatus.FAILED);
			booking.setStatus(BookingStatus.CANCELLED);

			if (!booking.isSeatsUnlocked()) {
				seatClient.unlockSeats(
						UnlockSeatsRequestDto.builder().showId(booking.getShowId()).userId(booking.getUserId())
								.sessionId(booking.getSeatSessionId()).seatNumbers(seatNumbers).build());
				booking.setSeatsUnlocked(true);
			}
		}

		bookingRepository.save(booking);
	}
}