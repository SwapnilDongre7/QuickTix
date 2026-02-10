package com.quicktix.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile("default") // or "local"
public class WebhookSignatureStartupLogger {

	@Value("${razorpay.webhook.secret}")
	private String webhookSecret;

	@PostConstruct
	public void logSampleWebhookSignature() {
		try {
			String samplePayload = """
					{
					  "event": "payment.captured",
					  "payload": {
					    "payment": {
					      "entity": {
					        "id": "pay_test_123",
					        "order_id": "order_test_456",
					        "status": "captured"
					      }
					    }
					  }
					}
					""".trim();

			String signature = generateHmacSHA256(samplePayload, webhookSecret);

			log.info("========== Razorpay Webhook Test Data ==========");
			log.info("Webhook Secret   : {}", webhookSecret);
			log.info("Sample Payload   : {}", samplePayload);
			log.info("Generated Signature (HEX): {}", signature);
			log.info("===============================================");

		} catch (Exception e) {
			log.error("Failed to generate sample webhook signature", e);
		}
	}

	private String generateHmacSHA256(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(keySpec);

		byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(rawHmac);
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hex = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

}