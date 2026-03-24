package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "emi_payments")
public class Emi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loanId;

    private double amountPaid;

    private double remainingAmount;

    private int totalEmis;

    private int paidEmis;

    private int remainingEmis;

    private String paymentMethod; // CASH / UPI

    private LocalDate paymentDate;

    private String status;
    // Constructors
    public Emi() {}

    public Emi(Long loanId, double amountPaid, double remainingAmount,
                      int totalEmis, int paidEmis, int remainingEmis,
                      String paymentMethod, LocalDate paymentDate) {
        this.loanId = loanId;
        this.amountPaid = amountPaid;
        this.remainingAmount = remainingAmount;
        this.totalEmis = totalEmis;
        this.paidEmis = paidEmis;
        this.remainingEmis = remainingEmis;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }

    // Getters & Setters
    public double getAmountPaid() {
        return amountPaid;
    }

    public Long getLoanId() {
        return loanId;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public int getTotalEmis() {
        return totalEmis;
    }

    public int getPaidEmis() {
        return paidEmis;
    }

    public int getRemainingEmis() {
        return remainingEmis;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void setTotalEmis(int totalEmis) {
        this.totalEmis = totalEmis;
    }

    public void setPaidEmis(int paidEmis) {
        this.paidEmis = paidEmis;
    }

    public void setRemainingEmis(int remainingEmis) {
        this.remainingEmis = remainingEmis;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}