package com.quicktix.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktix.payment.dto.request.RazorpayWebhookEvent;
import com.quicktix.payment.exception.InvalidSignatureException;
import com.quicktix.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(WebhookController.class)
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    // We mock ObjectMapper or use the one provided by Spring
    @Autowired
    private ObjectMapper objectMapper;

    private static final String WEBHOOK_URL = "/payments/webhook";
    private static final String VALID_PAYLOAD = "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_123\",\"order_id\":\"order_123\",\"status\":\"captured\"}}}}";
    private static final String VALID_SIGNATURE = "valid_signature";

    // --- A. Security & Signature Tests ---

    @Test
    void testValidSignatureAndPayload_ShouldReturn200() throws Exception {
        doNothing().when(paymentService).processWebhook(anyString(), anyString(), any(RazorpayWebhookEvent.class));

        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD)
                .header("X-Razorpay-Signature", VALID_SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService).processWebhook(eq(VALID_PAYLOAD), eq(VALID_SIGNATURE), any(RazorpayWebhookEvent.class));
    }

    @Test
    void testMissingSignatureHeader_ShouldReturn401_Or_BadRequest() throws Exception {
        // The controller throws InvalidSignatureException manually if header is missing
        // Typically this might result in 400 or 401 depending on
        // GlobalExceptionHandler.
        // Assuming GlobalExceptionHandler maps InvalidSignatureException to 400 or 401.
        // Based on typical REST practices for missing auth headers, 401 or 400 is
        // expected.
        // Let's assume the controller rethrows logic or exception handler handles it.
        // NOTE: The controller code explicitly checks for null signature and throws
        // exception.

        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD))
                .andExpect(status().is4xxClientError()); // Expecting 400 or 401
    }

    @Test
    void testEmptySignatureHeader_ShouldReturn401_Or_BadRequest() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD)
                .header("X-Razorpay-Signature", ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testInvalidSignature_ShouldReturn401_Or_Error() throws Exception {
        // Mock service throwing exception
        doThrow(new InvalidSignatureException("Invalid signature")).when(paymentService)
                .processWebhook(anyString(), anyString(), any(RazorpayWebhookEvent.class));

        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD)
                .header("X-Razorpay-Signature", "invalid_sig"))
                .andExpect(status().is4xxClientError()); // Should be mapped to 401/403/400
    }

    // --- C. Payload Validation ---

    @Test
    void testMalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{ \"event\": \"payment.captured\", \"payloa... "; // Broken JSON

        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
                .header("X-Razorpay-Signature", VALID_SIGNATURE))
                .andExpect(status().isBadRequest());
    }

    // --- H. Error Handling ---

    @Test
    void testServiceException_ShouldReturn200_ToPreventRetry() throws Exception {
        // If an unexpected exception occurs during processing (not signature related),
        // we should return 200 OK so Razorpay doesn't retry indefinitely.

        doThrow(new RuntimeException("Db error")).when(paymentService)
                .processWebhook(anyString(), anyString(), any(RazorpayWebhookEvent.class));

        mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD)
                .header("X-Razorpay-Signature", VALID_SIGNATURE))
                .andExpect(status().isOk()) // Controller catches generic Exception and returns 200
                .andExpect(jsonPath("$.success").value(false)); // Assuming ApiResponse.error sets success=false
    }
}
