package com.cdac.booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	Optional<Booking> findByIdempotencyKey(String idempotencyKey);

	Optional<Booking> findByIdAndUserId(Long id, Long userId);
}