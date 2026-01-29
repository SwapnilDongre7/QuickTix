package com.quicktix.payment.entity;

/**
 * Payment status enumeration
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED,
    EXPIRED
}
