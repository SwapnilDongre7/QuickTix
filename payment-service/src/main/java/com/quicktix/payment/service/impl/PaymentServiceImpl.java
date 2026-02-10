package com.quicktix.payment.service.impl;

import com.quicktix.payment.dto.request.InitiatePaymentRequest;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.dto.request.VerifyPaymentRequest;
import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.entity.Payment;
import com.quicktix.payment.entity.PaymentMode;
import com.quicktix.payment.entity.PaymentStatus;
import com.quicktix.payment.exception.*;
import com.quicktix.payment.mapper.PaymentMapper;
import com.quicktix.payment.repository.PaymentRepository;
import com.quicktix.payment.service.BookingNotificationService;
import com.quicktix.payment.service.PaymentService;
import com.quicktix.payment.service.RazorpayService;
import com.quicktix.payment.util.WebhookSignatureValidator;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Payment service implementation with Razorpay integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final BookingNotificationService bookingNotificationService;
    private final WebhookSignatureValidator signatureValidator;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(String idempotencyKey, InitiatePaymentRequest request) {
        log.info("Initiating payment: bookingId={}, amount={}, idempotencyKey={}",
                request.getBookingId(), request.getAmount(), idempotencyKey);

        // Check for existing payment with same idempotency key (duplicate prevention)
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    log.info("Found existing payment with idempotency key: {}", idempotencyKey);
                    return paymentMapper.toResponse(existing);
                })
                .orElseGet(() -> createNewPayment(idempotencyKey, request));
    }

    private PaymentResponse createNewPayment(String idempotencyKey, InitiatePaymentRequest request) {
        try {
            // Check if a successful payment already exists for this booking
            if (paymentRepository.existsByBookingIdAndStatus(request.getBookingId(), PaymentStatus.SUCCESS)) {
                throw new DuplicatePaymentException("Booking already has a successful payment");
            }

            // Create Razorpay order
            String receipt = "booking_" + request.getBookingId();
            String description = request.getDescription() != null ? request.getDescription() : "QuickTix Movie Booking";

            Order razorpayOrder = razorpayService.createOrder(
                    request.getAmount(),
                    request.getCurrency(),
                    receipt,
                    description);

            String orderId = razorpayOrder.get("id");
            String paymentUrl = razorpayService.generatePaymentUrl(orderId);

            // Create payment record
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(orderId)
                    .idempotencyKey(idempotencyKey)
                    .paymentUrl(paymentUrl)
                    // paymentMode will be set during verification when actual method is known
                    .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 min expiry
                    .build();

            payment = paymentRepository.save(payment);

            log.info("Payment created: id={}, razorpayOrderId={}", payment.getId(), orderId);

            return paymentMapper.toResponse(payment);

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void processWebhook(String payload, String signature, RazorpayWebhookEvent event) {
        log.info("Processing webhook event: {}", event.getEvent());

        // CRITICAL: Verify webhook signature to prevent fraud
        if (!signatureValidator.verifySignature(payload, signature)) {
            log.error("SECURITY ALERT: Invalid webhook signature detected!");
            throw new InvalidSignatureException();
        }

        log.info("Webhook signature verified successfully");

        // Process based on event type
        String eventType = event.getEvent();

        switch (eventType) {
            case "payment.captured":
                handlePaymentCaptured(event);
                break;
            case "payment.failed":
                handlePaymentFailed(event);
                break;
            case "order.paid":
                handleOrderPaid(event);
                break;
            default:
                log.info("Ignoring webhook event type: {}", eventType);
        }
    }

    private void handlePaymentCaptured(RazorpayWebhookEvent event) {
        var paymentEntity = event.getPayload().getPayment().getEntity();
        String orderId = paymentEntity.getOrderId();
        String paymentId = paymentEntity.getId();

        log.info("Payment captured: orderId={}, paymentId={}", orderId, paymentId);

        Payment payment = getPaymentByRazorpayOrderId(orderId);

        // Guard against duplicate callbacks
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already marked as success, ignoring duplicate callback");
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(paymentId);

        try {
            payment.setPaymentMode(PaymentMode.valueOf(paymentEntity.getMethod().toUpperCase()));
        } catch (Exception e) {
            log.warn("Unknown payment method: {}, setting to OTHER", paymentEntity.getMethod());
            payment.setPaymentMode(PaymentMode.OTHER);
        }

        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Notify booking service
        bookingNotificationService.notifyPaymentSuccess(payment.getBookingId(), payment.getId());

        log.info("Payment {} marked as SUCCESS", payment.getId());
    }

    private void handlePaymentFailed(RazorpayWebhookEvent event) {
        var paymentEntity = event.getPayload().getPayment().getEntity();
        String orderId = paymentEntity.getOrderId();
        String reason = paymentEntity.getErrorDescription();

        log.info("Payment failed: orderId={}, reason={}", orderId, reason);

        Payment payment = getPaymentByRazorpayOrderId(orderId);

        // Guard against duplicate callbacks
        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already in terminal state, ignoring callback");
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setRazorpayPaymentId(paymentEntity.getId());
        paymentRepository.save(payment);

        // Notify booking service
        bookingNotificationService.notifyPaymentFailure(payment.getBookingId(), payment.getId(), reason);

        log.info("Payment {} marked as FAILED", payment.getId());
    }

    private void handleOrderPaid(RazorpayWebhookEvent event) {
        // Order paid event is similar to payment captured
        var orderEntity = event.getPayload().getOrder().getEntity();
        String orderId = orderEntity.getId();

        log.info("Order paid: orderId={}", orderId);

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElse(null);

        if (payment == null) {
            log.warn("Payment not found for order: {}", orderId);
            return;
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            bookingNotificationService.notifyPaymentSuccess(payment.getBookingId(), payment.getId());
        }
    }

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Payment getPaymentByRazorpayOrderId(String orderId) {
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
    }

    /**
     * Verify payment after Razorpay checkout completion.
     * This is called by the frontend after the user completes payment on Razorpay.
     * 
     * Flow:
     * 1. Verify the signature to ensure the payment is authentic
     * 2. Find and update the payment record
     * 3. Notify the booking service of successful payment
     */
    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment: orderId={}, paymentId={}",
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        // 1. Verify Razorpay signature to prevent fraud
        boolean isValid = signatureValidator.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if (!isValid) {
            log.error("SECURITY ALERT: Invalid payment signature for order: {}",
                    request.getRazorpayOrderId());
            throw new InvalidSignatureException("Invalid payment signature");
        }

        log.info("Payment signature verified successfully");

        // 2. Find the payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + request.getRazorpayOrderId()));

        // 3. Guard against duplicate verification
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already verified and marked as success");
            return paymentMapper.toResponse(payment);
        }

        // 4. Update payment status
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setTransactionRef(request.getTransactionRef());
        payment.setCompletedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        log.info("Payment {} verified and marked as SUCCESS", payment.getId());

        // 5. Notify booking service of successful payment
        try {
            bookingNotificationService.notifyPaymentSuccess(payment.getBookingId(), payment.getId());
        } catch (Exception e) {
            // Log but don't fail - webhook will retry if needed
            log.error("Failed to notify booking service, will retry via webhook: {}", e.getMessage());
        }

        return paymentMapper.toResponse(payment);
    }

    /**
     * Mark payment as failed and notify booking service.
     * Called by frontend when user cancels Razorpay checkout or payment fails.
     */
    @Override
    @Transactional
    public void failPayment(Long bookingId, String failureReason) {
        log.info("Processing payment failure for bookingId={}, reason={}", bookingId, failureReason);

        // Find the most recent PENDING payment for this booking
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);

        Payment payment = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (payment == null) {
            log.warn("No pending payment found for bookingId={}, may already be processed", bookingId);
            // Still notify booking service in case seats need to be released
            try {
                bookingNotificationService.notifyPaymentFailure(bookingId, null, failureReason);
            } catch (Exception e) {
                log.error("Failed to notify booking service of failure: {}", e.getMessage());
            }
            return;
        }

        // Guard against already processed payments
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Payment {} already in terminal state {}, ignoring failure", payment.getId(), payment.getStatus());
            return;
        }

        // Update payment to FAILED
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        paymentRepository.save(payment);

        log.info("Payment {} marked as FAILED", payment.getId());

        // Notify booking service to cancel booking and release seats
        try {
            bookingNotificationService.notifyPaymentFailure(payment.getBookingId(), payment.getId(), failureReason);
        } catch (Exception e) {
            log.error("Failed to notify booking service of failure, will retry via scheduled job: {}", e.getMessage());
        }
    }
}
