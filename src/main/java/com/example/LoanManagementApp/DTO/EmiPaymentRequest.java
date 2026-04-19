package com.example.LoanManagementApp.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for EMI Payment Request
 * Accepts only necessary fields from client, avoiding nested object deserialization issues
 */
public class EmiPaymentRequest {

    @NotNull(message = "Loan ID cannot be null")
    private Long loanId;

    @NotNull(message = "Amount paid cannot be null")
    @DecimalMin(value = "0.01", message = "Amount paid must be greater than 0")
    private Double amountPaid;

    @NotBlank(message = "Payment method cannot be empty")
    @Pattern(regexp = "^(CASH|UPI|CHEQUE|ONLINE_TRANSFER)$", 
             message = "Payment method must be CASH, UPI, CHEQUE, or ONLINE_TRANSFER")
    private String paymentMethod = "CASH";

    // ==================== CONSTRUCTORS ====================

    public EmiPaymentRequest() {}

    public EmiPaymentRequest(Long loanId, Double amountPaid, String paymentMethod) {
        this.loanId = loanId;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
