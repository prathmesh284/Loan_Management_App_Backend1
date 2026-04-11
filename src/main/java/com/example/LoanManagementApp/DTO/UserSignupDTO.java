package com.example.LoanManagementApp.DTO;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for User Signup - Accepts branchName as String instead of Branch object
 * Simplifies frontend integration by allowing string branch selection
 */
public class UserSignupDTO {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian number")
    private String phoneNumber;

    @NotBlank(message = "Aadhar number cannot be empty")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be exactly 12 digits")
    private String adharNumber;

    @NotNull(message = "Date of birth cannot be null")
    private LocalDate dob;

    @NotBlank(message = "Gender cannot be empty")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @NotBlank(message = "Branch name cannot be empty")
    private String branch; // Accepts branch name from frontend (e.g., "Tanishq Jewellers")

    // ==================== CONSTRUCTORS ====================

    public UserSignupDTO() {
    }

    public UserSignupDTO(String username, String email, String password, String phoneNumber,
                         String adharNumber, LocalDate dob, String gender, String branch) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.adharNumber = adharNumber;
        this.dob = dob;
        this.gender = gender;
        this.branch = branch;
    }

    // ==================== GETTERS & SETTERS ====================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAdharNumber() {
        return adharNumber;
    }

    public void setAdharNumber(String adharNumber) {
        this.adharNumber = adharNumber;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
