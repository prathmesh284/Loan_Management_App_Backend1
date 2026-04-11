package com.example.LoanManagementApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.service.DashboardService;
import java.util.List;
import java.util.Map;

/**
 * DashboardController - REST endpoints for dashboard data and analytics
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard statistics for a branch
     * Returns: active loans, total customers, pending/overdue EMIs
     */
    @GetMapping("/stats/{branchId}")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@PathVariable Long branchId) {
        Map<String, Object> stats = dashboardService.getDashboardStats(branchId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent loans for a branch (last 5)
     */
    @GetMapping("/recent-loans/{branchId}")
    public ResponseEntity<List<Loan>> getRecentLoans(@PathVariable Long branchId) {
        List<Loan> recentLoans = dashboardService.getRecentLoans(branchId);
        return ResponseEntity.ok(recentLoans);
    }

    /**
     * Get loans due within next 30 days (for monitoring)
     */
    @GetMapping("/loans-due-soon/{branchId}")
    public ResponseEntity<List<Loan>> getLoansCloseToDue(@PathVariable Long branchId) {
        List<Loan> loansDue = dashboardService.getLoansCloseToDue(branchId);
        return ResponseEntity.ok(loansDue);
    }

    /**
     * Get loan history with pagination
     */
    @GetMapping("/loan-history/{branchId}")
    public ResponseEntity<List<Loan>> getLoanHistory(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Loan> loanHistory = dashboardService.getLoanHistory(branchId, page, size);
        return ResponseEntity.ok(loanHistory);
    }
}
