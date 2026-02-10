package com.quicktix.payment.dto.request;

import com.quicktix.payment.entity.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying Razorpay payment after checkout.
 * Called by frontend after successful Razorpay payment to verify signature
 * and confirm the payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;

    private PaymentMode paymentMode;

    private String transactionRef;
}
