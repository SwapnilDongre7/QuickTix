package com.quicktix.payment.service.impl;

import com.quicktix.payment.dto.request.InitiatePaymentRequest;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.entity.Payment;
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
                    .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 min expiry
                    .build();

            payment = paymentRepository.save(payment);

            log.info("Payment created: id={}, razorpayOrderId={}", payment.getId(), orderId);

            return paymentMapper.toResponse(payment);

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create payment order", e);
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
}
