package com.cdac.payment.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.payment.client.BookingClient;
import com.cdac.payment.entity.Payment;
import com.cdac.payment.entity.PaymentMode;
import com.cdac.payment.entity.PaymentStatus;
import com.cdac.payment.exception.PaymentNotFoundException;
import com.cdac.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final RazorpayClient razorpayClient;
	private final BookingClient bookingClient;

	@Value("${razorpay.webhook.secret}")
	private String webhookSecret;

	// =====================================================
	// 1️ CREATE PAYMENT (CALLED BY BOOKING SERVICE)
	// =====================================================
	@Transactional
	public Payment createPayment(String idempotencyKey, Long bookingId, Long userId, BigDecimal amount,
			PaymentMode paymentMode) {

		return paymentRepository.findByIdempotencyKey(idempotencyKey).orElseGet(() -> {

			try {
				JSONObject options = new JSONObject();
				options.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
				options.put("currency", "INR");
				options.put("receipt", "booking_" + bookingId);

				Order order = razorpayClient.orders.create(options);

				Payment payment = Payment.builder().bookingId(bookingId).userId(userId).amount(amount)
						.paymentMode(paymentMode).transactionRef(order.get("id")).status(PaymentStatus.PENDING)
						.idempotencyKey(idempotencyKey).build();

				return paymentRepository.save(payment);

			} catch (Exception e) {
				throw new RuntimeException("Failed to initiate payment", e);
			}
		});
	}

	// =====================================================
	// 2️ WEBHOOK ENTRY POINT
	// =====================================================
	public void processWebhook(String payload, String signature) throws Exception {

		// Signature verification can be enabled later

		JSONObject json = new JSONObject(payload);
		String event = json.getString("event");

		String orderId = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity")
				.getString("order_id");

		if ("payment.captured".equals(event)) {
			handlePaymentSuccess(orderId);
		}

		if ("payment.failed".equals(event)) {
			handlePaymentFailure(orderId);
		}
	}

	// =====================================================
	// 3️ SUCCESS FLOW
	// =====================================================
	@Transactional
	public void handlePaymentSuccess(String transactionRef) {

		Payment payment = paymentRepository.findByTransactionRef(transactionRef).orElseThrow(
				() -> new PaymentNotFoundException("Payment not found for transactionRef: " + transactionRef));

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return; // idempotent
		}

		payment.setStatus(PaymentStatus.SUCCESS);
		paymentRepository.save(payment);

		// Notify Booking Service
		bookingClient.notifyBooking(payment.getBookingId(), "SUCCESS");
	}

	// =====================================================
	// 4️ FAILURE FLOW
	// =====================================================
	@Transactional
	public void handlePaymentFailure(String transactionRef) {

		Payment payment = paymentRepository.findByTransactionRef(transactionRef).orElseThrow(
				() -> new PaymentNotFoundException("Payment not found for transactionRef: " + transactionRef));

		if (payment.getStatus() == PaymentStatus.FAILED) {
			return;
		}

		payment.setStatus(PaymentStatus.FAILED);
		paymentRepository.save(payment);

		//  Notify Booking Service
		bookingClient.notifyBooking(payment.getBookingId(), "FAILED");
	}
}