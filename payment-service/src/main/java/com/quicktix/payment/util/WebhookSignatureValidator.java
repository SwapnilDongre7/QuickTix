package com.quicktix.payment.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * CRITICAL: Webhook signature validator for Razorpay webhooks
 * 
 * This class verifies the X-Razorpay-Signature header to prevent payment fraud.
 * Without this verification, attackers could send fake webhook events to
 * confirm payments that were never made.
 * 
 * Algorithm: HMAC-SHA256
 * The signature is computed as: HMAC_SHA256(webhook_secret, payload_body)
 */
@Slf4j
@Component
public class WebhookSignatureValidator {

    private final String webhookSecret;
    private final String keySecret;
    private final boolean devMode;

    public WebhookSignatureValidator(
            @Value("${razorpay.webhook.secret}") String webhookSecret,
            @Value("${razorpay.key.secret}") String keySecret,
            @Value("${razorpay.webhook.dev-mode:false}") boolean devMode) {
        this.webhookSecret = webhookSecret;
        this.keySecret = keySecret;
        this.devMode = devMode;
        if (devMode) {
            log.warn("⚠️ WEBHOOK DEV MODE ENABLED - Signature validation is DISABLED! Do NOT use in production!");
        }
    }

    /**
     * Verify the Razorpay webhook signature
     * 
     * @param payload   The raw request body as string
     * @param signature The X-Razorpay-Signature header value
     * @return true if signature is valid, false otherwise
     * @throws SecurityException if signature verification fails
     */
    public boolean verifySignature(String payload, String signature) {
        // DEV MODE: Skip signature validation for testing
        if (devMode) {
            log.warn("DEV MODE: Skipping webhook signature validation!");
            return true;
        }

        if (payload == null || signature == null || webhookSecret == null) {
            log.error("Webhook signature validation failed: null parameters");
            return false;
        }

        try {
            String expectedSignature = computeHmacSha256(payload, webhookSecret);

            // Use constant-time comparison to prevent timing attacks
            boolean isValid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));

            if (!isValid) {
                log.warn("Webhook signature mismatch. Expected: {}, Received: {}",
                        expectedSignature.substring(0, 10) + "...",
                        signature.substring(0, Math.min(10, signature.length())) + "...");
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Compute HMAC-SHA256 hash
     * 
     * @param data   The data to hash
     * @param secret The secret key
     * @return Hex-encoded hash string
     */
    private String computeHmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        hmacSha256.init(secretKeySpec);

        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }

    /**
     * Verify payment signature for redirect callback
     * 
     * @param orderId   Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Razorpay signature
     * @param keySecret Razorpay key secret
     * @return true if valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId,
            String signature, String keySecret) {
        try {
            String data = orderId + "|" + paymentId;
            String expectedSignature = computeHmacSha256(data, keySecret);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error verifying payment signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify payment signature using Razorpay KEY SECRET.
     * This overload is for frontend verification calls after Razorpay checkout.
     * 
     * IMPORTANT: Payment signatures use key.secret, NOT webhook.secret!
     * 
     * @param orderId   Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Razorpay signature
     * @return true if valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        // DEV MODE: Skip signature validation for testing
        if (devMode) {
            log.warn("DEV MODE: Skipping payment signature validation!");
            return true;
        }

        // CRITICAL: Use keySecret for payment verification, not webhookSecret
        return verifyPaymentSignature(orderId, paymentId, signature, keySecret);
    }
}
