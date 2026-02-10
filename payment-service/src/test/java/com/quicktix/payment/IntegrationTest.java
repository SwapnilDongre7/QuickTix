package com.quicktix.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.entity.Payment;
import com.quicktix.payment.entity.PaymentStatus;
import com.quicktix.payment.repository.PaymentRepository;
import com.quicktix.payment.service.BookingNotificationService;
import com.quicktix.payment.util.WebhookSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingNotificationService bookingNotificationService;

    // We mock the validator to bypass HMAC calculation in tests,
    // or we could compute a real HMAC if we had the secret.
    @MockBean
    private WebhookSignatureValidator signatureValidator;

    private static final String ORDER_ID = "order_integration_123";

    @BeforeEach
    void setup() {
        paymentRepository.deleteAll();

        // Seed database
        Payment payment = Payment.builder()
                .bookingId(999L)
                .userId(888L)
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .razorpayOrderId(ORDER_ID)
                .idempotencyKey("idem_key_1")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        paymentRepository.save(payment);

        // Allow any signature for integration test simplicity
        when(signatureValidator.verifySignature(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void testEndToEnd_PaymentCaptured_ShouldUpdateDb() throws Exception {
        // Construct Payload
        RazorpayWebhookEvent event = new RazorpayWebhookEvent();
        event.setEvent("payment.captured");

        RazorpayWebhookEvent.Payload payload = new RazorpayWebhookEvent.Payload();
        RazorpayWebhookEvent.PaymentWrapper paymentWrapper = new RazorpayWebhookEvent.PaymentWrapper();
        RazorpayWebhookEvent.PaymentEntity paymentEntity = new RazorpayWebhookEvent.PaymentEntity();

        paymentEntity.setId("pay_integration_999");
        paymentEntity.setOrderId(ORDER_ID);
        paymentEntity.setStatus("captured");

        paymentWrapper.setEntity(paymentEntity);
        payload.setPayment(paymentWrapper);
        event.setPayload(payload);

        String jsonPayload = objectMapper.writeValueAsString(event);

        // Perform Request
        mockMvc.perform(post("/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("X-Razorpay-Signature", "dummy_sig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify Database
        Payment updatedPayment = paymentRepository.findByRazorpayOrderId(ORDER_ID).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertEquals("pay_integration_999", updatedPayment.getRazorpayPaymentId());
    }
}
