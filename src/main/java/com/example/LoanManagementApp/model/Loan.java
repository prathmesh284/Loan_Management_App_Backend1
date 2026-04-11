package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Loan Entity - Represents gold loan applications
 * Maintains relationships with Customer and EMI payments
 */
@Entity
@Table(name = "loans")
public class Loan {

    // ==================== FIELDS ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false)
    @NotNull(message = "Customer cannot be null")
    private Customer customer;

    @NotBlank(message = "Gold type cannot be empty")
    @Pattern(regexp = "^(22K|23K|24K)$", message = "Gold type must be 22K, 23K, or 24K")
    @Column(nullable = false)
    private String goldType;

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0")
    @Column(nullable = false)
    private Double weight;

    @NotNull(message = "Gold price cannot be null")
    @DecimalMin(value = "1", message = "Gold price must be greater than 0")
    @Column(nullable = false)
    private Double goldPrice;

    @NotNull(message = "LTV cannot be null")
    @DecimalMin(value = "1", message = "LTV must be greater than 0")
    @DecimalMax(value = "100", message = "LTV cannot exceed 100")
    @Column(nullable = false)
    private Double ltv;

    @NotNull(message = "Interest rate cannot be null")
    @DecimalMin(value = "0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100", message = "Interest rate cannot exceed 100")
    @Column(nullable = false)
    private Double interestRate;

    @NotNull(message = "Tenure cannot be null")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 84, message = "Tenure cannot exceed 84 months")
    @Column(nullable = false)
    private Integer tenure;

    @NotNull(message = "Loan amount cannot be null")
    @DecimalMin(value = "1000", message = "Loan amount must be at least 1000")
    @Column(nullable = false)
    private Double loanAmount;

    @NotNull(message = "EMI cannot be null")
    @DecimalMin(value = "0", message = "EMI cannot be negative")
    @Column(nullable = false)
    private Double emi;

    @NotNull(message = "Total interest cannot be null")
    @DecimalMin(value = "0", message = "Total interest cannot be negative")
    @Column(nullable = false)
    private Double totalInterest;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0", message = "Total amount cannot be negative")
    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double paidAmount = 0.0;

    @Column(nullable = false)
    private Double remainingAmount = 0.0;

    @Column(nullable = false)
    private Integer totalEmis = 0;

    @Column(nullable = false)
    private Integer paidEmis = 0;

    @Column(nullable = false)
    private Integer remainingEmis = 0;

    @Column(nullable = false)
    private LocalDate loanDate = LocalDate.now();

    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Emi> emiPayments;

    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(ACTIVE|CLOSED|DEFAULTED)$", message = "Status must be ACTIVE, CLOSED, or DEFAULTED")
    @Column(nullable = false)
    private String status = "ACTIVE";

    // ==================== CONSTRUCTORS ====================

    public Loan() {}

    public Loan(Customer customer, String goldType, Double weight, Double goldPrice,
                Double ltv, Double interestRate, Integer tenure, Double loanAmount,
                Double emi, Double totalInterest, Double totalAmount) {
        this.customer = customer;
        this.goldType = goldType;
        this.weight = weight;
        this.goldPrice = goldPrice;
        this.ltv = ltv;
        this.interestRate = interestRate;
        this.tenure = tenure;
        this.loanAmount = loanAmount;
        this.emi = emi;
        this.totalInterest = totalInterest;
        this.totalAmount = totalAmount;
        this.loanDate = LocalDate.now();
        this.remainingAmount = loanAmount;
        this.totalEmis = tenure;
        this.remainingEmis = tenure;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getGoldType() {
        return goldType;
    }

    public void setGoldType(String goldType) {
        this.goldType = goldType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getGoldPrice() {
        return goldPrice;
    }

    public void setGoldPrice(Double goldPrice) {
        this.goldPrice = goldPrice;
    }

    public Double getLtv() {
        return ltv;
    }

    public void setLtv(Double ltv) {
        this.ltv = ltv;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenure() {
        return tenure;
    }

    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Double getEmi() {
        return emi;
    }

    public void setEmi(Double emi) {
        this.emi = emi;
    }

    public Double getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(Double totalInterest) {
        this.totalInterest = totalInterest;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
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

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getNextEmiDate() {
        return nextEmiDate;
    }

    public void setNextEmiDate(LocalDate nextEmiDate) {
        this.nextEmiDate = nextEmiDate;
    }

    public List<Emi> getEmiPayments() {
        return emiPayments;
    }

    public void setEmiPayments(List<Emi> emiPayments) {
        this.emiPayments = emiPayments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getName() : "null") +
                ", goldType='" + goldType + '\'' +
                ", weight=" + weight +
                ", loanAmount=" + loanAmount +
                ", status='" + status + '\'' +
                '}';
    }
}

