package com.cdac.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for ticket details.
 * Contains all information needed by frontend to display a complete ticket
 * without requiring multiple API calls.
 * 
 * This DTO is populated via microservice orchestration in BookingService,
 * aggregating data from ShowSeat, Catalogue, and Theatre services.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponse {

    private Long id;
    private String movieName;
    private String theatreName;
    private String screenName;
    private LocalDateTime showTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String status; // BookingStatus as String
    private String paymentStatus; // PaymentStatus as String

    // Additional ticket display fields
    private String ticketId;
    private String qrCodeData;
    private String language;
    private String format;

    // Timestamps
    private LocalDateTime bookedAt;
}