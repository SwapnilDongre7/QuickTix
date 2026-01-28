package com.cdac.payment.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RazorpaySignatureUtil {

	public static boolean verifySignature(String payload, String actualSignature, String secret) throws Exception {

		Mac sha256Hmac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		sha256Hmac.init(keySpec);

		byte[] hash = sha256Hmac.doFinal(payload.getBytes());
		String expectedSignature = Base64.getEncoder().encodeToString(hash);

		return expectedSignature.equals(actualSignature);
	}
}
