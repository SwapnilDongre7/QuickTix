package com.cdac.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BookingClient {

	private final RestTemplate restTemplate = new RestTemplate();

	public void notifyBooking(Long bookingId, String paymentStatus) {

		String url = "http://localhost:8084/bookings/payment-status";

		restTemplate.postForEntity(url, Map.of("bookingId", bookingId, "paymentStatus", paymentStatus), Void.class);
	}
}