package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.DTO.PaymentRequest;
import com.example.LoanManagementApp.DTO.PaymentResponse;
import com.example.LoanManagementApp.service.EmiService;
import com.example.LoanManagementApp.service.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment Gateway Controller
 * Handles payment gateway webhooks and confirmations (Razorpay, PayPal, etc.)
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    @Autowired
    private EmiService emiService;

    @Autowired(required = false)
    private PaymentGatewayService paymentGatewayService;

    /**
     * Webhook endpoint for Razorpay payment callbacks
     * Called by Razorpay after payment completion
     */
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Map<String, Object>> razorpayWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Razorpay webhook received: {}", payload);

        try {
            // Verify signature
            if (!verifyRazorpaySignature(payload, signature)) {
                log.warn("Invalid Razorpay signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid signature"));
            }

            Map<String, Object> event = (Map<String, Object>) payload.get("event");
            Map<String, Object> paymentData = (Map<String, Object>) payload.get("payload");

            if ("payment.authorized".equals(event) || "payment.captured".equals(event)) {
                handlePaymentSuccess(paymentData);
            } else if ("payment.failed".equals(event)) {
                handlePaymentFailure(paymentData);
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Webhook endpoint for PayPal payment callbacks
     */
    @PostMapping("/webhook/paypal")
    public ResponseEntity<Map<String, Object>> paypalWebhook(@RequestBody Map<String, Object> payload) {
        log.info("PayPal webhook received");

        try {
            // Handle PayPal webhook events
            String eventType = (String) payload.get("event_type");

            if ("CHECKOUT.ORDER.COMPLETED".equals(eventType)) {
                handlePaymentSuccess(payload);
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Error processing PayPal webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Verify payment status from gateway
     */
    @GetMapping("/verify/{transactionId}")
    public ResponseEntity<Map<String, Object>> verifyPayment(@PathVariable String transactionId) {
        log.info("Verifying payment: {}", transactionId);

        try {
            if (paymentGatewayService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("success", false, "message", "Payment gateway not configured"));
            }

            String status = paymentGatewayService.getPaymentStatus(transactionId);
            boolean verified = "captured".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", verified);
            response.put("transactionId", transactionId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get payment details
     */
    @GetMapping("/details/{transactionId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(@PathVariable String transactionId) {
        log.info("Fetching payment details: {}", transactionId);

        try {
            if (paymentGatewayService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("success", false, "message", "Payment gateway not configured"));
            }

            Map<String, Object> details = paymentGatewayService.getPaymentDetails(transactionId);

            return ResponseEntity.ok(Map.of(
                    "success", !details.isEmpty(),
                    "details", details
            ));

        } catch (Exception e) {
            log.error("Error fetching payment details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Refund a payment
     */
    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<Map<String, Object>> refundPayment(
            @PathVariable String transactionId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {

        log.info("Refunding payment: {} amount: {}", transactionId, amount);

        try {
            if (paymentGatewayService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("success", false, "message", "Payment gateway not configured"));
            }

            String refundId = paymentGatewayService.refundPayment(transactionId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", refundId != null);
            response.put("originalTransactionId", transactionId);
            response.put("refundId", refundId);
            response.put("amount", amount);
            response.put("reason", reason);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Initiate payment (create order/link)
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            @RequestParam String customerId,
            @RequestParam(defaultValue = "razorpay") String gateway) {

        log.info("Initiating {} payment for loan: {} amount: {}", gateway, loanId, amount);

        try {
            if (paymentGatewayService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("success", false, "message", "Payment gateway not configured"));
            }

            String orderId = generateOrderId(loanId);
            String paymentLink = paymentGatewayService.createPaymentLink(amount, customerId, loanId, orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", paymentLink != null);
            response.put("loanId", loanId);
            response.put("amount", amount);
            response.put("orderId", orderId);
            response.put("paymentLink", paymentLink);
            response.put("gateway", gateway);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error initiating payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Health check endpoint for payment gateway
     */
    @GetMapping("/gateway-status")
    public ResponseEntity<Map<String, Object>> gatewayStatus() {
        log.info("Checking payment gateway status");

        Map<String, Object> response = new HashMap<>();

        if (paymentGatewayService == null) {
            response.put("status", "UNAVAILABLE");
            response.put("message", "Payment gateway service not configured");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        response.put("status", "AVAILABLE");
        response.put("gateway", paymentGatewayService.getGatewayName());
        response.put("message", "Payment gateway is operational");

        return ResponseEntity.ok(response);
    }

    /**
     * Handle successful payment
     */
    private void handlePaymentSuccess(Map<String, Object> paymentData) {
        log.info("Processing successful payment");

        try {
            // Extract payment details from gateway response
            String transactionId = extractTransactionId(paymentData);
            String customerId = extractCustomerId(paymentData);
            String loanId = extractLoanId(paymentData);
            BigDecimal amount = extractAmount(paymentData);

            log.info("Payment successful: transactionId={} loanId={} amount={}", transactionId, loanId, amount);

            // Process payment through EMI service
            PaymentRequest request = new PaymentRequest();
            request.setLoanId(Long.parseLong(loanId));
            request.setAmount(amount);
            request.setPaymentMethod("ONLINE_TRANSFER");
            request.setPaymentMode("RAZORPAY");
            request.setPaymentId(transactionId);
            request.setGenerateReceipt(true);

            emiService.processPayment(request);

        } catch (Exception e) {
            log.error("Error handling payment success", e);
        }
    }

    /**
     * Handle failed payment
     */
    private void handlePaymentFailure(Map<String, Object> paymentData) {
        log.warn("Processing failed payment: {}", paymentData);
        // Log failure, send notification to customer, etc.
    }

    /**
     * Verify Razorpay signature
     */
    private boolean verifyRazorpaySignature(Map<String, Object> payload, String signature) {
        // TODO: Implement HMAC-SHA256 signature verification
        // For now, return true - implement proper verification in production
        return true;
    }

    /**
     * Generate order ID
     */
    private String generateOrderId(Long loanId) {
        return String.format("ORD_%d_%d", loanId, System.currentTimeMillis());
    }

    /**
     * Extract transaction ID from payment data
     */
    private String extractTransactionId(Map<String, Object> data) {
        Object payment = data.get("payment");
        if (payment instanceof Map) {
            return (String) ((Map<String, Object>) payment).get("id");
        }
        return (String) data.get("id");
    }

    /**
     * Extract customer ID from payment data
     */
    private String extractCustomerId(Map<String, Object> data) {
        Object payment = data.get("payment");
        if (payment instanceof Map) {
            Map<String, Object> paymentMap = (Map<String, Object>) payment;
            Map<String, Object> notes = (Map<String, Object>) paymentMap.get("notes");
            if (notes != null) {
                return (String) notes.get("customer_id");
            }
        }
        return "";
    }

    /**
     * Extract loan ID from payment data
     */
    private String extractLoanId(Map<String, Object> data) {
        Object payment = data.get("payment");
        if (payment instanceof Map) {
            Map<String, Object> paymentMap = (Map<String, Object>) payment;
            Map<String, Object> notes = (Map<String, Object>) paymentMap.get("notes");
            if (notes != null) {
                return (String) notes.get("loan_id");
            }
        }
        return "";
    }

    /**
     * Extract amount from payment data
     */
    private BigDecimal extractAmount(Map<String, Object> data) {
        Object payment = data.get("payment");
        if (payment instanceof Map) {
            Map<String, Object> paymentMap = (Map<String, Object>) payment;
            Object amountInPaise = paymentMap.get("amount");
            if (amountInPaise instanceof Number) {
                return BigDecimal.valueOf(((Number) amountInPaise).longValue()).divide(
                        BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }
}
