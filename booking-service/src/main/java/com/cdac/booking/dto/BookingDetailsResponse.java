package com.cdac.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for booking details retrieval.
 * Includes all booking information needed by the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsResponse {

    private Long id;
    private Long userId;
    private String showId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;

    // Ticket-related fields for frontend display
    private String ticketId;
    private String qrCodeData;

    // Show-related fields (populated via service enrichment if needed)
    private String movieName;
    private String theatreName;
    private String screenName;
    private String sessionId; // Helper for frontend confirmations
    private LocalDateTime showTime;
}
