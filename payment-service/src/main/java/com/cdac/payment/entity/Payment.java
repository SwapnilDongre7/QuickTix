package com.cdac.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_id", nullable = false)
	private Long bookingId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_mode", nullable = false)
	private PaymentMode paymentMode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(name = "transaction_ref", unique = true)
	private String transactionRef;

	@Column(name = "idempotency_key", nullable = false, unique = true)
	private String idempotencyKey;

	@Builder.Default
	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

}
