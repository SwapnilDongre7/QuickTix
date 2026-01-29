package com.quicktix.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.dto.response.ApiResponse;
import com.quicktix.payment.exception.InvalidSignatureException;
import com.quicktix.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook controller for Razorpay callbacks
 * 
 * CRITICAL SECURITY: This controller handles payment webhooks.
 * All requests MUST be verified using the X-Razorpay-Signature header.
 */
@Slf4j
@RestController
@RequestMapping("/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * Handle Razorpay webhook callbacks
     * 
     * SECURITY: Signature verification is performed in the service layer
     * to ensure the request is authentic.
     * 
     * @param payload   Raw request body (needed for signature verification)
     * @param signature X-Razorpay-Signature header
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("POST /payments/webhook - Received webhook event");

        // CRITICAL: Reject requests without signature header
        if (signature == null || signature.isEmpty()) {
            log.error("SECURITY ALERT: Webhook request without signature header!");
            throw new InvalidSignatureException("Missing X-Razorpay-Signature header");
        }

        try {
            // Parse the webhook event
            RazorpayWebhookEvent event = objectMapper.readValue(payload, RazorpayWebhookEvent.class);

            log.info("Webhook event type: {}", event.getEvent());

            // Process webhook (signature verification happens in service)
            paymentService.processWebhook(payload, signature, event);

            // Always return 200 OK to Razorpay (they retry on failure)
            return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully", null));

        } catch (InvalidSignatureException e) {
            // Re-throw signature exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            // Still return 200 to prevent Razorpay retries for processing errors
            // The error is logged for investigation
            return ResponseEntity.ok(ApiResponse.error("Webhook processing error logged"));
        }
    }

    /**
     * Webhook health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> webhookHealth() {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint healthy", "OK"));
    }
}
