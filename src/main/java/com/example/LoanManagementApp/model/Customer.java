package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Customer Entity - Represents loan customers/applicants
 * Maintains relationships with Branch, Loans, and Documents
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "customer_id"),
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "aadhar_number"),
    @UniqueConstraint(columnNames = "pan_number")
})
public class Customer {

    // ==================== FIELDS ====================

    @Id
    @NotBlank(message = "Customer ID (phone) cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Customer ID must be a valid 10-digit Indian phone number")
    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;  // phone number (unique ID, not auto-generated)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    @NotNull(message = "Branch cannot be null")
    private Branch branch;

    @NotBlank(message = "Customer name cannot be empty")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Aadhar number cannot be empty")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be exactly 12 digits")
    @Column(name = "aadhar_number", nullable = false, unique = true)
    private String aadharNumber;

    @NotBlank(message = "PAN number cannot be empty")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number format invalid. Example: AAAAA0000A")
    @Column(name = "pan_number", nullable = false, unique = true)
    private String panNumber;

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Loan> loans;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Document> documents;

    // ==================== CONSTRUCTORS ====================

    public Customer() {
        // default constructor
    }

    public Customer(String customerId, Branch branch, String name, String email,
                    String aadharNumber, String panNumber, String address) {
        this.customerId = customerId;
        this.branch = branch;
        this.name = name;
        this.email = email;
        this.aadharNumber = aadharNumber;
        this.panNumber = panNumber;
        this.address = address;
    }

    // ==================== GETTERS & SETTERS ====================

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", aadharNumber='" + aadharNumber + '\'' +
                ", panNumber='" + panNumber + '\'' +
                '}';
    }
}
