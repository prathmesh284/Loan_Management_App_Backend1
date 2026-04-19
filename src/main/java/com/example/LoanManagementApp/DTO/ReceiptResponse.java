package com.example.LoanManagementApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Receipt Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private Long receiptId;

    private String receiptNumber;

    private Long emiId;

    private Long loanId;

    private Long customerId;

    private String customerName;

    private BigDecimal amountPaid;

    private String paymentMethod;

    private String paymentMode;

    private String transactionId;

    private String status; // PENDING, CONFIRMED, FAILED

    private LocalDateTime issuedDate;

    private LocalDateTime paidDate;

    private String receiptPath;

    private String remarks;

    // EMI Details
    private BigDecimal emiAmount;

    private Integer paidEmis;

    private Integer remainingEmis;

    private BigDecimal remainingLoanAmount;

    // Loan Details
    private BigDecimal loanAmount;

    private BigDecimal totalAmount;

    private Integer totalEmis;

    private Integer emiTenure; // Loan tenure in months
}
