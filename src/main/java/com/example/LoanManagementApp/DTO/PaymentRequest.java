package com.example.LoanManagementApp.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Enhanced Payment Request DTO for both Cash and Online payments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "CASH|UPI|CHEQUE|ONLINE_TRANSFER", 
            message = "Payment method must be CASH, UPI, CHEQUE, or ONLINE_TRANSFER")
    private String paymentMethod;

    @NotNull(message = "Payment mode is required")
    @Pattern(regexp = "MANUAL|RAZORPAY|PAYPAL|STRIPE|BANK_TRANSFER",
            message = "Payment mode must be MANUAL, RAZORPAY, PAYPAL, STRIPE, or BANK_TRANSFER")
    private String paymentMode;

    // Fields for online payments
    @Pattern(regexp = "^[a-zA-Z0-9]{10,20}$|^$", 
            message = "Razorpay Key ID is invalid (if provided)")
    private String razorpayKeyId; // Provided by frontend from Razorpay checkout

    @Pattern(regexp = "^[a-zA-Z0-9]{15,30}$|^$",
            message = "Razorpay Order ID is invalid (if provided)")
    private String razorpayOrderId; // Created by payment gateway

    @Pattern(regexp = "^[a-zA-Z0-9]{15,30}$|^$",
            message = "Payment ID is invalid (if provided)")
    private String paymentId; // Transaction ID from payment gateway

    @Pattern(regexp = "^[a-zA-Z0-9]{10,50}$|^$",
            message = "Signature is invalid (if provided)")
    private String signature; // Razorpay signature for verification

    // Fields for check payments
    @Size(max = 20, message = "Check number cannot exceed 20 characters")
    private String checkNumber;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    // Optional remarks
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @Size(max = 100, message = "Receipt number cannot exceed 100 characters")
    private String receiptNumber;

    // For automatic receipt generation
    private Boolean generateReceipt = true;

    // For auto-scheduling future EMIs (if payment mode is online)
    private Boolean autoScheduleNextPayment = false;

    // Validate that required fields are present based on payment type
    public boolean isValidForPaymentType() {
        if ("RAZORPAY".equals(paymentMode) || "PAYPAL".equals(paymentMode) || "STRIPE".equals(paymentMode)) {
            return paymentId != null && !paymentId.isEmpty();
        }
        if ("CHEQUE".equals(paymentMethod)) {
            return checkNumber != null && !checkNumber.isEmpty() && bankName != null && !bankName.isEmpty();
        }
        return true; // CASH is always valid
    }
}
