package com.example.LoanManagementApp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;   // phone number
    private String customerName;
    private String docType;      // KYC / Loan / Gold
    private String s3Url;        // stored file URL

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }


    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDocType() {
        return docType;
    }

    public String getS3Url() {
        return s3Url;
    }
}