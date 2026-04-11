package com.example.LoanManagementApp.DTO;

import jakarta.validation.constraints.*;

/**
 * DTO for User Login - Only contains username and password
 * Prevents validation errors from requiring signup-only fields (email, phone, aadhar, etc.)
 */
public class LoginDTO {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // ==================== CONSTRUCTORS ====================

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ==================== GETTERS & SETTERS ====================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
