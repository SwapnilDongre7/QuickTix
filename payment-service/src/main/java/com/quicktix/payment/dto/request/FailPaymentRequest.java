package com.quicktix.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reporting payment failure/cancellation.
 * Called by frontend when user cancels Razorpay checkout or payment fails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailPaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String failureReason;
}
