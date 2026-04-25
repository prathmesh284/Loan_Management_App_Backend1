package com.example.LoanManagementApp.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Payment Gateway Service Interface
 * Abstract interface for handling different payment gateways
 */
public interface PaymentGatewayService {

    /**
     * Create a payment link/order for online payment
     * @param amount Payment amount
     * @param customerId Customer identifier
     * @param loanId Loan identifier
     * @param orderId Unique order identifier
     * @return Payment link/URL for customer
     */
    default String createPaymentLink(BigDecimal amount, String customerId, Long loanId, String orderId) {
        return createPaymentLink(amount, customerId, loanId, orderId, Collections.emptyMap());
    }

    /**
     * Create a payment link/order for online payment with gateway-specific options
     * @param amount Payment amount
     * @param customerId Customer identifier
     * @param loanId Loan identifier
     * @param orderId Unique order identifier
     * @param options Gateway-specific options such as payment method or receipt number
     * @return Payment link/URL for customer
     */
    String createPaymentLink(BigDecimal amount, String customerId, Long loanId, String orderId, Map<String, Object> options);

    /**
     * Verify payment from gateway
     * @param transactionId Gateway transaction ID
     * @param orderId Our system's order ID
     * @return true if payment verified successfully
     */
    boolean verifyPayment(String transactionId, String orderId);

    /**
     * Get payment details from gateway
     * @param transactionId Gateway transaction ID
     * @return Map containing payment details
     */
    Map<String, Object> getPaymentDetails(String transactionId);

    /**
     * Refund a payment
     * @param transactionId Original transaction ID
     * @param amount Amount to refund
     * @return Refund transaction ID
     */
    String refundPayment(String transactionId, BigDecimal amount);

    /**
     * Check payment status
     * @param transactionId Gateway transaction ID
     * @return Payment status (COMPLETED, PENDING, FAILED, etc.)
     */
    String getPaymentStatus(String transactionId);

    /**
     * Get gateway name
     * @return Gateway identifier (e.g., "razorpay", "paypal")
     */
    String getGatewayName();
}
