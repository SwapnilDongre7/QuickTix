package com.cdac.booking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.booking.dto.BookingDetailsResponse;
import com.cdac.booking.dto.TicketResponse;

import com.cdac.booking.client.PaymentClient;
import com.cdac.booking.client.PricingClient;
import com.cdac.booking.client.SeatClient;
import com.cdac.booking.dto.CreateBookingRequest;
import com.cdac.booking.dto.CreateBookingResponse;
import com.cdac.booking.dto.seat.ApiResponseDto;
import com.cdac.booking.dto.seat.ConfirmSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsRequestDto;
import com.cdac.booking.dto.seat.LockSeatsResponseDto;
import com.cdac.booking.dto.seat.UnlockSeatsRequestDto;
import com.cdac.booking.entity.Booking;
import com.cdac.booking.entity.BookingSeat;
import com.cdac.booking.entity.BookingStatus;
import com.cdac.booking.entity.PaymentStatus;
import com.cdac.booking.exception.BookingNotFoundException;
import com.cdac.booking.exception.SeatLockFailedException;
import com.cdac.booking.feign.CatalogueFeignClient;
import com.cdac.booking.feign.ShowSeatFeignClient;
import com.cdac.booking.feign.TheatreFeignClient;
import com.cdac.booking.feign.dto.MovieResponseDto;
import com.cdac.booking.feign.dto.ScreenResponseDto;
import com.cdac.booking.feign.dto.ShowResponseDto;
import com.cdac.booking.feign.dto.TheatreResponseDto;
import com.cdac.booking.repository.BookingRepository;
import com.cdac.booking.repository.BookingSeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Booking service with dynamic pricing support and ticket enrichment.
 * 
 * Prices are fetched from ShowSeat service via PricingClient.
 * Falls back to default pricing if pricing service is unavailable.
 * 
 * Ticket details are enriched via Feign calls to ShowSeat, Catalogue, and
 * Theatre services.
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

	// Feign clients for ticket enrichment
	private final ShowSeatFeignClient showSeatFeignClient;
	private final CatalogueFeignClient catalogueFeignClient;
	private final TheatreFeignClient theatreFeignClient;

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
	public void handlePaymentUpdate(Long bookingId, String paymentStatus, Long paymentId, String failureReason) {
		log.info("Processing payment update: bookingId={}, status={}, paymentId={}, reason={}",
				bookingId, paymentStatus, paymentId, failureReason);

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

	// =========================
	// GET BOOKING DETAILS
	// =========================

	/**
	 * Get booking details by ID
	 * Returns full booking information including seat numbers for ticket display
	 */
	@Transactional(readOnly = true)
	public BookingDetailsResponse getBookingDetails(Long bookingId) {
		log.info("Fetching booking details for bookingId={}", bookingId);

		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

		List<String> seatNumbers = bookingSeatRepository.findByBookingId(bookingId)
				.stream()
				.map(BookingSeat::getSeatNo)
				.collect(Collectors.toList());

		return buildBookingDetailsResponse(booking, seatNumbers);
	}

	/**
	 * Get all bookings for a user
	 */
	@Transactional(readOnly = true)
	public List<BookingDetailsResponse> getUserBookings(Long userId) {
		log.info("Fetching bookings for userId={}", userId);

		return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(booking -> {
					List<String> seatNumbers = bookingSeatRepository.findByBookingId(booking.getId())
							.stream()
							.map(BookingSeat::getSeatNo)
							.collect(Collectors.toList());
					return buildBookingDetailsResponse(booking, seatNumbers);
				})
				.collect(Collectors.toList());
	}

	/**
	 * Get seat numbers for a booking
	 */
	@Transactional(readOnly = true)
	public List<String> getBookingSeats(Long bookingId) {
		log.info("Fetching seats for bookingId={}", bookingId);

		// Verify booking exists
		if (!bookingRepository.existsById(bookingId)) {
			throw new BookingNotFoundException(bookingId);
		}

		return bookingSeatRepository.findByBookingId(bookingId)
				.stream()
				.map(BookingSeat::getSeatNo)
				.collect(Collectors.toList());
	}

	/**
	 * Build BookingDetailsResponse from entity
	 */
	private BookingDetailsResponse buildBookingDetailsResponse(Booking booking, List<String> seatNumbers) {
		// Generate ticket ID and QR code data
		String ticketId = "TKT-" + booking.getId();
		String qrCodeData = String.format("QUICKTIX-%d-%s-%d",
				booking.getId(),
				booking.getShowId(),
				booking.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC));

		return BookingDetailsResponse.builder()
				.id(booking.getId())
				.userId(booking.getUserId())
				.showId(booking.getShowId())
				.sessionId(booking.getSeatSessionId())
				.seatNumbers(seatNumbers)
				.totalAmount(booking.getTotalAmount())
				.status(booking.getStatus().name())
				.paymentStatus(booking.getPaymentStatus().name())
				.createdAt(booking.getCreatedAt())
				.ticketId(ticketId)
				.qrCodeData(qrCodeData)
				// Note: movieName, theatreName, etc. would need enrichment from show service
				// For now, frontend can fetch show details separately or we can add this later
				.build();
	}

	// =========================
	// GET TICKET (ENRICHED)
	// =========================

	/**
	 * Get enriched ticket details for a booking.
	 * Orchestrates calls to ShowSeat, Catalogue, and Theatre services
	 * to build a complete TicketResponse.
	 * 
	 * @param bookingId The booking ID
	 * @return TicketResponse with all enriched data (partial data if some services
	 *         unavailable)
	 */
	@Transactional(readOnly = true)
	public TicketResponse getTicket(Long bookingId) {
		log.info("Fetching enriched ticket for bookingId={}", bookingId);

		// 1. Fetch booking from MySQL (required - will throw if not found)
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		// 2. Fetch seat numbers from MySQL
		List<String> seatNumbers = bookingSeatRepository.findByBookingId(bookingId)
				.stream()
				.map(BookingSeat::getSeatNo)
				.collect(Collectors.toList());

		// 3. Fetch show details from ShowSeat service (required for IDs)
		ShowResponseDto show = null;
		try {
			ApiResponseDto<ShowResponseDto> showResponse = showSeatFeignClient.getShowById(booking.getShowId());
			show = showResponse.getData();
		} catch (Exception e) {
			log.error("Failed to fetch show details for showId={}: {}", booking.getShowId(), e.getMessage());
		}

		// 4. Fetch movie details from Catalogue service (graceful degradation)
		String movieName = "Movie Unavailable";
		if (show != null && show.getMovieId() != null) {
			try {
				MovieResponseDto movie = catalogueFeignClient.getMovieById(show.getMovieId());
				if (movie != null && movie.getTitle() != null) {
					movieName = movie.getTitle();
				}
			} catch (Exception e) {
				log.warn("Catalogue service unavailable for movieId={}: {}", show.getMovieId(), e.getMessage());
			}
		}

		// 5. Fetch theatre details from Theatre service (graceful degradation)
		String theatreName = "Theatre Unavailable";
		if (show != null && show.getTheatreId() != null) {
			try {
				TheatreResponseDto theatre = theatreFeignClient.getTheatreById(show.getTheatreId());
				if (theatre != null && theatre.getName() != null) {
					theatreName = theatre.getName();
				}
			} catch (Exception e) {
				log.warn("Theatre service unavailable for theatreId={}: {}", show.getTheatreId(), e.getMessage());
			}
		}

		// 6. Fetch screen details from Theatre service (graceful degradation)
		String screenName = "Screen Unavailable";
		if (show != null && show.getScreenId() != null) {
			try {
				ScreenResponseDto screen = theatreFeignClient.getScreenById(show.getScreenId());
				if (screen != null && screen.getName() != null) {
					screenName = screen.getName();
				}
			} catch (Exception e) {
				log.warn("Theatre service unavailable for screenId={}: {}", show.getScreenId(), e.getMessage());
			}
		}

		// 7. Generate ticket ID and QR code
		String ticketId = "TKT-" + booking.getId();
		String qrCodeData = String.format("QUICKTIX-%d-%s-%d",
				booking.getId(),
				booking.getShowId(),
				booking.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC));

		// 8. Build and return enriched TicketResponse (with partial data if needed)
		log.info("Building ticket response for bookingId={} (showAvailable={})",
				bookingId, show != null);

		return TicketResponse.builder()
				.id(booking.getId())
				.movieName(movieName)
				.theatreName(theatreName)
				.screenName(screenName)
				.showTime(show != null ? show.getStartTime() : null)
				.seatNumbers(seatNumbers)
				.totalAmount(booking.getTotalAmount())
				.status(booking.getStatus().name())
				.paymentStatus(booking.getPaymentStatus().name())
				.ticketId(ticketId)
				.qrCodeData(qrCodeData)
				.language(show != null ? show.getLanguage() : null)
				.format(show != null ? show.getFormat() : null)
				.bookedAt(booking.getCreatedAt())
				.build();
	}
}