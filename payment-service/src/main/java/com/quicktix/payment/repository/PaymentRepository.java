package com.quicktix.payment.repository;

import com.quicktix.payment.entity.Payment;
import com.quicktix.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment repository for database operations
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by idempotency key (for duplicate prevention)
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payment by Razorpay order ID
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Find payment by Razorpay payment ID
     */
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Find all payments for a booking
     */
    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Find payments by user
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find expired pending payments for cleanup
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, @Param("now") LocalDateTime now);

    /**
     * Check if a successful payment exists for a booking
     */
    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
