package com.quicktix.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity - stores payment transaction details
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_booking_id", columnList = "bookingId"),
        @Index(name = "idx_razorpay_order_id", columnList = "razorpayOrderId"),
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String razorpayOrderId;

    @Column(length = 100)
    private String razorpayPaymentId;

    @Column(length = 200)
    private String razorpaySignature;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 500)
    private String paymentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 50)
    private PaymentMode paymentMode;

    @Column(length = 100)
    private String transactionRef;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime completedAt;
}
