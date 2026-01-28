package com.cdac.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByBookingId(Long bookingId);

	Optional<Payment> findByTransactionRef(String transactionRef);

	Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
