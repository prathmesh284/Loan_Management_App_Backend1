package com.example.LoanManagementApp.DTO;

public class LoginResponse {
    private String token;
    private Long branchId;

    public LoginResponse(String token, Long branchId) {
        this.token = token;
        this.branchId = branchId;
    }

    public String getToken() {
        return token;
    }

    public Long getBranchId() {
        return branchId;
    }
}
