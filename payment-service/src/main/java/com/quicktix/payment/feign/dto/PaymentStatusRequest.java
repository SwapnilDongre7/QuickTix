package com.quicktix.payment.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating payment status in booking service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusRequest {
    private Long bookingId;
    private String paymentStatus;
    private Long paymentId;
    private String failureReason;
}
