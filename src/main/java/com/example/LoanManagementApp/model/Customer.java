package com.example.LoanManagementApp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    // ----------------- FIELDS -----------------

    @Id
    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;  // phone number (unique ID, not auto-generated)

    @Column(name = "branch_id")
    private Long branchId;

    private String name;

    private String email;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "pan_number")
    private String panNumber;

    private String address;



    // ----------------- GETTERS & SETTERS -----------------

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
    	this.branchId = branchId;
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


    // ----------------- CONSTRUCTORS -----------------

    public Customer() {
        // default constructor
    }

    public Customer(String customerId, Long branchId, String name, String email,
                    String aadharNumber, String panNumber, String address, String imagePath) {
        this.customerId = customerId;
        this.branchId = branchId;
        this.name = name;
        this.email = email;
        this.aadharNumber = aadharNumber;
        this.panNumber = panNumber;
        this.address = address;
    }
}
