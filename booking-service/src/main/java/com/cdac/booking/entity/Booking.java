package com.cdac.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private String showId;

	@Column(name = "seat_session_id", nullable = false)
	private String seatSessionId;

	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	private BookingStatus status;

	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	@Column(nullable = false, unique = true)
	private String idempotencyKey;

	@Column(name = "seats_confirmed")
	private boolean seatsConfirmed;

	@Column(name = "seats_unlocked")
	private boolean seatsUnlocked;

	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
}