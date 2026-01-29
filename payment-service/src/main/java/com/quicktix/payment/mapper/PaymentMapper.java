package com.quicktix.payment.mapper;

import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.entity.Payment;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Payment entity and DTOs
 */
@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .paymentUrl(payment.getPaymentUrl())
                .createdAt(payment.getCreatedAt())
                .expiresAt(payment.getExpiresAt())
                .build();
    }
}
