package com.quicktix.payment.service.impl;

import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.dto.response.PaymentResponse;
import com.quicktix.payment.entity.Payment;
import com.quicktix.payment.entity.PaymentStatus;
import com.quicktix.payment.exception.InvalidSignatureException;
import com.quicktix.payment.mapper.PaymentMapper;
import com.quicktix.payment.repository.PaymentRepository;
import com.quicktix.payment.service.BookingNotificationService;
import com.quicktix.payment.service.RazorpayService;
import com.quicktix.payment.util.WebhookSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private BookingNotificationService bookingNotificationService;

    @Mock
    private WebhookSignatureValidator signatureValidator;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final String VALID_PAYLOAD = "{}";
    private static final String VALID_SIGNATURE = "sig";
    private static final String ORDER_ID = "order_123";
    private static final String PAYMENT_ID = "pay_123";

    private RazorpayWebhookEvent capturedEvent;
    private RazorpayWebhookEvent failedEvent;

    @BeforeEach
    void setUp() {
        // Setup Captured Event
        capturedEvent = new RazorpayWebhookEvent();
        capturedEvent.setEvent("payment.captured");
        RazorpayWebhookEvent.Payload payload = new RazorpayWebhookEvent.Payload();
        RazorpayWebhookEvent.PaymentWrapper paymentWrapper = new RazorpayWebhookEvent.PaymentWrapper();
        RazorpayWebhookEvent.PaymentEntity paymentEntity = new RazorpayWebhookEvent.PaymentEntity();
        paymentEntity.setId(PAYMENT_ID);
        paymentEntity.setOrderId(ORDER_ID);
        paymentEntity.setStatus("captured");
        paymentWrapper.setEntity(paymentEntity);
        payload.setPayment(paymentWrapper);
        capturedEvent.setPayload(payload);

        // Setup Failed Event
        failedEvent = new RazorpayWebhookEvent();
        failedEvent.setEvent("payment.failed");
        failedEvent.setPayload(payload); // Reusing payload structure but would need error details ideally
        paymentEntity.setErrorDescription("Bank failure");
    }

    // --- A. Signature Security Cases ---

    @Test
    void testProcessWebhook_ValidSignature_ShouldProceed() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId(ORDER_ID)).thenReturn(Optional.of(createPendingPayment()));

        assertDoesNotThrow(() -> paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, capturedEvent));

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testProcessWebhook_InvalidSignature_ShouldThrowException() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(false);

        assertThrows(InvalidSignatureException.class,
                () -> paymentService.processWebhook(VALID_PAYLOAD, "invalid_sig", capturedEvent));

        verify(paymentRepository, never()).save(any());
    }

    // --- B. Event Type Handling & D. Database Consistency ---

    @Test
    void testPaymentCaptured_ShouldMarkSuccess_AndNotifyBooking() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        Payment pendingPayment = createPendingPayment();
        when(paymentRepository.findByRazorpayOrderId(ORDER_ID)).thenReturn(Optional.of(pendingPayment));

        paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, capturedEvent);

        assertEquals(PaymentStatus.SUCCESS, pendingPayment.getStatus());
        assertEquals(PAYMENT_ID, pendingPayment.getRazorpayPaymentId());
        verify(paymentRepository).save(pendingPayment);
        verify(bookingNotificationService).notifyPaymentSuccess(pendingPayment.getBookingId(), pendingPayment.getId());
    }

    @Test
    void testPaymentFailed_ShouldMarkFailed_AndNotifyBooking() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        Payment pendingPayment = createPendingPayment();
        when(paymentRepository.findByRazorpayOrderId(ORDER_ID)).thenReturn(Optional.of(pendingPayment));

        paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, failedEvent);

        assertEquals(PaymentStatus.FAILED, pendingPayment.getStatus());
        assertEquals("Bank failure", pendingPayment.getFailureReason());
        verify(paymentRepository).save(pendingPayment);
        verify(bookingNotificationService).notifyPaymentFailure(eq(pendingPayment.getBookingId()),
                eq(pendingPayment.getId()), anyString());
    }

    // --- G. Idempotency ---

    @Test
    void testPaymentCaptured_DuplicateWebhook_ShouldBeIdempotent() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        Payment successPayment = createPendingPayment();
        successPayment.setStatus(PaymentStatus.SUCCESS); // Already success

        when(paymentRepository.findByRazorpayOrderId(ORDER_ID)).thenReturn(Optional.of(successPayment));

        paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, capturedEvent);

        // Should NOT save again nor notify again (assuming implementation guards this)
        verify(paymentRepository, never()).save(any());
        verify(bookingNotificationService, never()).notifyPaymentSuccess(any(), any());
    }

    @Test
    void testPaymentFailed_DuplicateWebhook_ShouldBeIdempotent() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        Payment failedPayment = createPendingPayment();
        failedPayment.setStatus(PaymentStatus.FAILED); // Already failed

        when(paymentRepository.findByRazorpayOrderId(ORDER_ID)).thenReturn(Optional.of(failedPayment));

        paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, failedEvent);

        verify(paymentRepository, never()).save(any());
        verify(bookingNotificationService, never()).notifyPaymentFailure(any(), any(), any());
    }

    // --- E. Logic Validation ---

    @Test
    void testUnknownEvent_ShouldIgnoreGracefully() {
        when(signatureValidator.verifySignature(any(), any())).thenReturn(true);
        RazorpayWebhookEvent unknownEvent = new RazorpayWebhookEvent();
        unknownEvent.setEvent("unknown.event");

        assertDoesNotThrow(() -> paymentService.processWebhook(VALID_PAYLOAD, VALID_SIGNATURE, unknownEvent));

        verify(paymentRepository, never()).findByRazorpayOrderId(any());
        verify(paymentRepository, never()).save(any());
    }

    private Payment createPendingPayment() {
        return Payment.builder()
                .id(1L)
                .bookingId(100L)
                .razorpayOrderId(ORDER_ID)
                .status(PaymentStatus.PENDING)
                .amount(BigDecimal.valueOf(500))
                .build();
    }
}
