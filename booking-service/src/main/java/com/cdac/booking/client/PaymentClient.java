package com.cdac.booking.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentClient {

	private final RestTemplate restTemplate = new RestTemplate();

	public void initiatePayment(Long bookingId, Long userId, BigDecimal amount, String idempotencyKey) {

		String url = "http://localhost:8085/payments/initiate";

		Map<String, Object> body = Map.of("bookingId", bookingId, "userId", userId, "amount", amount, "paymentMode",
				"UPI");

		restTemplate.postForEntity(url,
				org.springframework.http.RequestEntity.post(url).header("Idempotency-Key", idempotencyKey).body(body),
				Object.class);
	}
}