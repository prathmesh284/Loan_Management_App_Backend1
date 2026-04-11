package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Branch Entity - Represents physical branches of the loan management organization
 * Maintains structured relationship with Users and Customers
 */
@Entity
@Table(name = "branches", uniqueConstraints = {
    @UniqueConstraint(columnNames = "branchCode")
})
public class Branch {

    // ==================== FIELDS ====================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Branch code cannot be empty")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{3}$", message = "Branch code must follow format: 3 uppercase letters + 3 digits (e.g., MUM001)")
    @Column(name = "branch_code", nullable = false, unique = true)
    private String branchCode;

    @NotBlank(message = "Branch name cannot be empty")
    @Size(min = 3, max = 100, message = "Branch name must be between 3 and 100 characters")
    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @NotBlank(message = "City cannot be empty")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State cannot be empty")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Manager name cannot be empty")
    @Size(min = 3, max = 100, message = "Manager name must be between 3 and 100 characters")
    @Column(name = "manager_name", nullable = false)
    private String managerName;

    @NotBlank(message = "Contact number cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Contact number must be a valid 10-digit Indian phone number")
    @Column(name = "contact_number", nullable = false, unique = true)
    private String contactNumber;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Boolean isActive = true;

    // ==================== CONSTRUCTORS ====================

    public Branch() {}

    public Branch(String branchCode, String branchName, String city, String state,
                  String managerName, String contactNumber, String email, String address) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.city = city;
        this.state = state;
        this.managerName = managerName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.isActive = true;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", branchName='" + branchName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", managerName='" + managerName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
