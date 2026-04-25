package com.example.LoanManagementApp.service.impl;

import com.example.LoanManagementApp.service.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Razorpay Payment Gateway Implementation
 * Handles payment creation, verification, refunds through Razorpay API
 */
@Slf4j
@Service
public class RazorpayPaymentGatewayService implements PaymentGatewayService {

    private static final String RAZORPAY_BASE_URL = "https://api.razorpay.com/v1";
    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${app.callback.url:http://localhost:8080}")
    private String callbackUrl;

    private final RestTemplate restTemplate;

    public RazorpayPaymentGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String createPaymentLink(BigDecimal amount, String customerId, Long loanId, String orderId) {
        return createPaymentLink(amount, customerId, loanId, orderId, Collections.emptyMap());
    }

    @Override
    public String createPaymentLink(BigDecimal amount, String customerId, Long loanId, String orderId, Map<String, Object> options) {
        log.info("Creating Razorpay payment link for customer: {} loan: {} order: {}", customerId, loanId, orderId);

        try {
            // Convert amount to paise (Razorpay uses paise)
            long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
            boolean isUpiPayment = "UPI".equalsIgnoreCase(stringValue(options.get("paymentMethod")));
            String preferredUpiApp = stringValue(options.get("upiApp"));
            String receiptNumber = stringValue(options.get("receiptNumber"));

            Map<String, Object> paymentLinkDetails = new HashMap<>();
            paymentLinkDetails.put("amount", amountInPaise);
            paymentLinkDetails.put("currency", "INR");
            paymentLinkDetails.put("accept_partial", false);
            if (isUpiPayment) {
                paymentLinkDetails.put("upi_link", true);
            }
            paymentLinkDetails.put("reference_id", orderId);
            paymentLinkDetails.put("description", "Loan EMI payment for loan " + loanId);
            paymentLinkDetails.put("callback_url", callbackUrl);
            paymentLinkDetails.put("callback_method", "get");
            Map<String, Object> notes = new HashMap<>();
            notes.put("customer_id", customerId);
            notes.put("loan_id", loanId.toString());
            notes.put("order_id", orderId);
            notes.put("payment_method", isUpiPayment ? "UPI" : "ONLINE_TRANSFER");
            if (!preferredUpiApp.isBlank()) {
                notes.put("preferred_upi_app", preferredUpiApp);
            }
            if (!receiptNumber.isBlank()) {
                notes.put("receipt_number", receiptNumber);
            }
            paymentLinkDetails.put("notes", notes);

            String paymentLinkUrl = RAZORPAY_BASE_URL + "/payment_links";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentLinkDetails, getAuthHeaders());
            ResponseEntity<Map> response = restTemplate.postForEntity(paymentLinkUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> paymentLinkResponse = response.getBody();
                String razorpayPaymentLinkId = (String) paymentLinkResponse.get("id");
                String paymentLink = stringValue(paymentLinkResponse.get("short_url"));
                log.info("Payment link created successfully: {} type={}", razorpayPaymentLinkId, isUpiPayment ? "UPI" : "STANDARD");
                return paymentLink.isBlank() ? null : paymentLink;
            } else {
                log.error("Failed to create Razorpay payment link: {}", response.getStatusCode());
                throw new IllegalStateException("Razorpay returned " + response.getStatusCode() + " while creating payment link");
            }
        } catch (ResourceAccessException e) {
            log.error("Error creating Razorpay payment link", e);
            throw new IllegalStateException(
                    "Could not connect to Razorpay from backend. Check AWS Lambda internet access, NAT gateway, route tables, and security-group egress.",
                    e
            );
        } catch (HttpStatusCodeException e) {
            log.error("Error creating Razorpay payment link", e);
            String responseBody = e.getResponseBodyAsString();
            throw new IllegalStateException(
                    "Razorpay rejected the payment link request: " + (responseBody == null || responseBody.isBlank() ? e.getStatusCode() : responseBody),
                    e
            );
        } catch (Exception e) {
            log.error("Error creating Razorpay payment link", e);
            throw new IllegalStateException("Failed to create Razorpay payment link: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPayment(String transactionId, String orderId) {
        log.info("Verifying payment: transaction={} order={}", transactionId, orderId);

        try {
            // Fetch order details from Razorpay
            String orderUrl = RAZORPAY_BASE_URL + "/orders/" + orderId;
            HttpEntity<Void> request = new HttpEntity<>(getAuthHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(orderUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> orderDetails = response.getBody();
                String status = (String) orderDetails.get("status");
                int amountPaid = (int) orderDetails.get("amount_paid");

                boolean isVerified = "paid".equalsIgnoreCase(status) && amountPaid > 0;
                log.info("Payment verification result: {}", isVerified ? "SUCCESS" : "FAILED");
                return isVerified;
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getPaymentDetails(String transactionId) {
        log.info("Fetching payment details for transaction: {}", transactionId);

        try {
            String paymentUrl = RAZORPAY_BASE_URL + "/payments/" + transactionId;
            HttpEntity<Void> request = new HttpEntity<>(getAuthHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(paymentUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error fetching payment details", e);
        }
        return new HashMap<>();
    }

    @Override
    public String refundPayment(String transactionId, BigDecimal amount) {
        log.info("Initiating refund for transaction: {} amount: {}", transactionId, amount);

        try {
            long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

            Map<String, Object> refundDetails = new HashMap<>();
            refundDetails.put("amount", amountInPaise);
            refundDetails.put("notes", Map.of(
                    "refund_reason", "Customer requested",
                    "refund_timestamp", System.currentTimeMillis()
            ));

            String refundUrl = RAZORPAY_BASE_URL + "/payments/" + transactionId + "/refund";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(refundDetails, getAuthHeaders());
            ResponseEntity<Map> response = restTemplate.postForEntity(refundUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String refundId = (String) response.getBody().get("id");
                log.info("Refund initiated successfully: {}", refundId);
                return refundId;
            }
        } catch (Exception e) {
            log.error("Error initiating refund", e);
        }
        return null;
    }

    @Override
    public String getPaymentStatus(String transactionId) {
        log.info("Checking payment status for transaction: {}", transactionId);

        try {
            Map<String, Object> details = getPaymentDetails(transactionId);
            if (!details.isEmpty()) {
                String status = (String) details.get("status");
                log.info("Payment status: {}", status);
                return status;
            }
        } catch (Exception e) {
            log.error("Error checking payment status", e);
        }
        return "UNKNOWN";
    }

    @Override
    public String getGatewayName() {
        return "razorpay";
    }

    /**
     * Get HTTP headers with basic authentication for Razorpay API
     */
    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Basic auth: key_id:key_secret
        String auth = razorpayKeyId + ":" + razorpayKeySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        return headers;
    }

    /**
     * Check if gateway is properly configured
     */
    public boolean isConfigured() {
        return razorpayKeyId != null && !razorpayKeyId.isEmpty()
                && razorpayKeySecret != null && !razorpayKeySecret.isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
