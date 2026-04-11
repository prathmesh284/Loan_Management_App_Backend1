package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Document Entity - Represents customer documents (KYC, Loan, Gold)
 * Maintains relationship with Customer for document tracking
 */
@Entity
@Table(name = "documents")
public class Document {

    // ==================== FIELDS ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false)
    @NotNull(message = "Customer cannot be null")
    private Customer customer;

    @NotBlank(message = "Document type cannot be empty")
    @Pattern(regexp = "^(KYC|LOAN|GOLD|PROPERTY|IDENTITY|ADDRESS|INCOME)$", 
             message = "Document type must be KYC, LOAN, GOLD, PROPERTY, IDENTITY, ADDRESS, or INCOME")
    @Column(name = "doc_type", nullable = false)
    private String docType;

    @NotBlank(message = "S3 URL cannot be empty")
    @Column(name = "s3_url", nullable = false)
    private String s3Url;

    @Column(name = "upload_date", nullable = false)
    private LocalDate uploadDate = LocalDate.now();

    @NotBlank(message = "Document name cannot be empty")
    @Size(min = 3, max = 100, message = "Document name must be between 3 and 100 characters")
    @Column(name = "doc_name", nullable = false)
    private String docName;

    @Column(name = "doc_description")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String docDescription;

    @NotNull(message = "Verification status cannot be null")
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    // ==================== CONSTRUCTORS ====================

    public Document() {}

    public Document(Customer customer, String docType, String s3Url, String docName) {
        this.customer = customer;
        this.docType = docType;
        this.s3Url = s3Url;
        this.docName = docName;
        this.uploadDate = LocalDate.now();
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

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocDescription() {
        return docDescription;
    }

    public void setDocDescription(String docDescription) {
        this.docDescription = docDescription;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDate getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDate verificationDate) {
        this.verificationDate = verificationDate;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", customerName=" + (customer != null ? customer.getName() : "null") +
                ", docType='" + docType + '\'' +
                ", docName='" + docName + '\'' +
                ", isVerified=" + isVerified +
                '}';
    }
}