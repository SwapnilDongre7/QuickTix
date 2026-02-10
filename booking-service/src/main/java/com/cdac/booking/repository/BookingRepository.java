package com.cdac.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cdac.booking.entity.Booking;
import com.cdac.booking.entity.BookingStatus;

/**
 * Booking repository for database operations
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	Optional<Booking> findByIdempotencyKey(String idempotencyKey);

	Optional<Booking> findByIdAndUserId(Long id, Long userId);

	/**
	 * Find abandoned bookings (INITIATED status older than cutoff time)
	 * Used by cleanup scheduler to expire stale bookings
	 */
	@Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :cutoff")
	List<Booking> findByStatusAndCreatedAtBefore(
			@Param("status") BookingStatus status,
			@Param("cutoff") LocalDateTime cutoff);

	/**
	 * Find all bookings for a user
	 */
	List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

	/**
	 * Find bookings by status
	 */
	List<Booking> findByStatus(BookingStatus status);

	/**
	 * Count bookings by status (for monitoring)
	 */
	long countByStatus(BookingStatus status);
}