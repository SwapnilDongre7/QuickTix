package com.quicktix.payment.dto.request;

import lombok.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Razorpay webhook payload DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RazorpayWebhookEvent {

    private String entity;
    private String accountId;
    private String event;
    private Boolean contains;
    private Payload payload;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Payload {
        private PaymentWrapper payment;
        private OrderWrapper order;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentWrapper {
        private PaymentEntity entity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderWrapper {
        private OrderEntity entity;
    } 

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentEntity {
        private String id;
        private String entity;
        private Long amount;
        private String currency;
        private String status;
        private String orderId;
        private String method;
        private String description;
        private String errorCode;
        private String errorDescription;
        private String errorReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderEntity {
        private String id;
        private String entity;
        private Long amount;
        private String currency;
        private String status;
        private String receipt;
    }
}
