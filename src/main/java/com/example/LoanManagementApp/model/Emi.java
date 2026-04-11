package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * EMI Entity - Represents EMI payment records
 * Maintains relationship with Loan for payment tracking
 */
@Entity
@Table(name = "emi_payments")
public class Emi {

    // ==================== FIELDS ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_id", nullable = false)
    @NotNull(message = "Loan cannot be null")
    private Loan loan;

    @NotNull(message = "Amount paid cannot be null")
    @DecimalMin(value = "0", message = "Amount paid cannot be negative")
    @Column(nullable = false)
    private Double amountPaid;

    @NotNull(message = "Remaining amount cannot be null")
    @DecimalMin(value = "0", message = "Remaining amount cannot be negative")
    @Column(nullable = false)
    private Double remainingAmount;

    @NotNull(message = "Total EMIs cannot be null")
    @Min(value = 1, message = "Total EMIs must be at least 1")
    @Column(nullable = false)
    private Integer totalEmis;

    @NotNull(message = "Paid EMIs cannot be null")
    @Min(value = 0, message = "Paid EMIs cannot be negative")
    @Column(nullable = false)
    private Integer paidEmis;

    @NotNull(message = "Remaining EMIs cannot be null")
    @Min(value = 0, message = "Remaining EMIs cannot be negative")
    @Column(nullable = false)
    private Integer remainingEmis;

    @NotBlank(message = "Payment method cannot be empty")
    @Pattern(regexp = "^(CASH|UPI|CHEQUE|ONLINE_TRANSFER)$", 
             message = "Payment method must be CASH, UPI, CHEQUE, or ONLINE_TRANSFER")
    @Column(nullable = false)
    private String paymentMethod;

    @NotNull(message = "Payment date cannot be null")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate = LocalDate.now();

    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "^(PENDING|PAID|OVERDUE|DEFAULT)$", 
             message = "Status must be PENDING, PAID, OVERDUE, or DEFAULT")
    @Column(nullable = false)
    private String status = "PENDING";

    // ==================== CONSTRUCTORS ====================

    public Emi() {}

    public Emi(Loan loan, Double amountPaid, Double remainingAmount,
               Integer totalEmis, Integer paidEmis, Integer remainingEmis,
               String paymentMethod, LocalDate paymentDate) {
        this.loan = loan;
        this.amountPaid = amountPaid;
        this.remainingAmount = remainingAmount;
        this.totalEmis = totalEmis;
        this.paidEmis = paidEmis;
        this.remainingEmis = remainingEmis;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Integer getTotalEmis() {
        return totalEmis;
    }

    public void setTotalEmis(Integer totalEmis) {
        this.totalEmis = totalEmis;
    }

    public Integer getPaidEmis() {
        return paidEmis;
    }

    public void setPaidEmis(Integer paidEmis) {
        this.paidEmis = paidEmis;
    }

    public Integer getRemainingEmis() {
        return remainingEmis;
    }

    public void setRemainingEmis(Integer remainingEmis) {
        this.remainingEmis = remainingEmis;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Emi{" +
                "id=" + id +
                ", loan=" + (loan != null ? loan.getId() : "null") +
                ", amountPaid=" + amountPaid +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentDate=" + paymentDate +
                ", status='" + status + '\'' +
                '}';
    }
}