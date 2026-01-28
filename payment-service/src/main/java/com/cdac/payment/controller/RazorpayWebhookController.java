package com.cdac.payment.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<String> handleWebhook(HttpServletRequest request,
			@RequestHeader("X-Razorpay-Signature") String signature) throws Exception {

		String payload;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {

			payload = reader.lines().collect(Collectors.joining("\n"));
		}

		paymentService.processWebhook(payload, signature);

		return ResponseEntity.ok("Webhook processed");
	}

}
