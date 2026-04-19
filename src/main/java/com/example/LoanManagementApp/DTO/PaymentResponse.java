package com.example.LoanManagementApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO - Response for payment creation or verification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Boolean success;

    private String message;

    private String status; // PENDING, COMPLETED, FAILED

    private Long loanId;

    private Long emiId;

    // Payment Details
    private BigDecimal amountPaid;

    private String paymentMethod;

    private String paymentMode;

    private String transactionId;

    private String paymentLink; // For online payments

    private LocalDateTime paymentTime;

    // Receipt Information
    private Long receiptId;

    private String receiptNumber;

    private String receiptPath; // Path to generated PDF receipt

    // Updated EMI Status
    private BigDecimal remainingAmount;

    private Integer paidEmis;

    private Integer remainingEmis;

    private String emiStatus; // PENDING, PAID, OVERDUE

    // Loan Status
    private String loanStatus; // ACTIVE, CLOSED, DEFAULTED

    private BigDecimal totalRemainingAmount;

    // Error Details (if failed)
    private String errorCode;

    private String errorMessage;

    // Gateway-specific responses
    private Object gatewayResponse; // Additional gateway-specific data

    // Next EMI Information
    private LocalDateTime nextEmiDate;

    private BigDecimal nextEmiAmount;
}
