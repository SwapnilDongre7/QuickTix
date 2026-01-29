package com.quicktix.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Razorpay integration service
 */
@Slf4j
@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final String keyId;

    public RazorpayService(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret) throws RazorpayException {
        this.keyId = keyId;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        log.info("RazorpayService initialized with key: {}...", keyId.substring(0, Math.min(10, keyId.length())));
    }

    /**
     * Create a Razorpay order
     * 
     * @param amount   Amount in INR (will be converted to paise)
     * @param currency Currency code
     * @param receipt  Receipt ID (usually booking ID)
     * @param notes    Additional notes
     * @return Razorpay Order object
     */
    public Order createOrder(BigDecimal amount, String currency, String receipt, String notes)
            throws RazorpayException {

        // Razorpay expects amount in smallest currency unit (paise for INR)
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("notes", new JSONObject().put("description", notes));

        log.info("Creating Razorpay order: amount={} paise, receipt={}", amountInPaise, receipt);

        Order order = razorpayClient.orders.create(orderRequest);

        log.info("Razorpay order created: id={}, status={}", order.get("id"), order.get("status"));

        return order;
    }

    /**
     * Generate payment URL for the order
     * 
     * @param orderId Razorpay order ID
     * @return Payment checkout URL
     */
    public String generatePaymentUrl(String orderId) {
        // Standard Razorpay checkout URL format
        return String.format("https://api.razorpay.com/v1/checkout/embedded?key_id=%s&order_id=%s",
                keyId, orderId);
    }

    /**
     * Fetch order details from Razorpay
     * 
     * @param orderId Razorpay order ID
     * @return Order details
     */
    public Order fetchOrder(String orderId) throws RazorpayException {
        return razorpayClient.orders.fetch(orderId);
    }

    public String getKeyId() {
        return keyId;
    }
}
