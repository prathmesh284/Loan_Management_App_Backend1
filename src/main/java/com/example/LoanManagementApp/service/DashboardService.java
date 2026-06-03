package com.example.LoanManagementApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.LoanManagementApp.config.ResourceNotFoundException;
import com.example.LoanManagementApp.model.Branch;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.repo.EmiRepo;
import com.example.LoanManagementApp.repo.LoanRepo;
import com.example.LoanManagementApp.repo.BranchRepo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardService - Provides data for dashboard analytics and statistics
 */
@Service
public class DashboardService {

    @Autowired
    private LoanRepo loanRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private EmiRepo emiRepo;

    @Autowired
    private BranchRepo branchRepo;

    /**
     * Get dashboard statistics for a branch
     * Returns: active loans, total customers, pending EMIs, overdue EMIs
     */
    public Map<String, Object> getDashboardStats(Long branchId) {
        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));

        Map<String, Object> stats = new HashMap<>();

        // Get all active loans for the branch
        List<Loan> activeLoans = loanRepo.findByBranchId(branchId);
        stats.put("activeLoanCount", (long) activeLoans.size());

        // Get total amount lent
        double totalAmountLent = activeLoans.stream()
                .mapToDouble(Loan::getLoanAmount)
                .sum();
        stats.put("totalAmountLent", totalAmountLent);

        // Get total customers for the branch
        List<Customer> customers = customerRepo.findByBranch(branch);
        stats.put("totalCustomers", (long) customers.size());

        // Get pending EMIs (unpaid)
        List<Emi> pendingEmis = emiRepo.findPendingEmis();
        stats.put("pendingEmis", (long) pendingEmis.size());

        // Get overdue EMIs
        List<Emi> overdueEmis = emiRepo.findOverdueEmis(LocalDate.now());
        stats.put("overdueEmis", (long) overdueEmis.size());

        return stats;
    }

    /**
     * Get recent loans for dashboard (last 5 loans)
     */
    public List<Loan> getRecentLoans(Long branchId) {
        return loanRepo.findRecentLoansByBranch(branchId, 5);
    }

    /**
     * Get loans due within next 30 days (for monitoring)
     */
    public List<Loan> getLoansCloseToDue(Long branchId) {
        return getLoansCloseToDue(branchId, 30);
    }

    /**
     * Get loans due within the next N days.
     */
    public List<Loan> getLoansCloseToDue(Long branchId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate upperBoundDate = today.plusDays(Math.max(days, 0));
        
        // Get all active loans for branch
        List<Loan> activeLoans = loanRepo.findByBranchId(branchId);
        
        // Filter loans where nextEmiDate is within the requested number of days
        return activeLoans.stream()
                .filter(loan -> {
                    LocalDate nextEmiDate = loan.getNextEmiDate();
                    return nextEmiDate != null 
                        && !nextEmiDate.isBefore(today)
                        && !nextEmiDate.isAfter(upperBoundDate);
                })
                .toList();
    }

    /**
     * Get loan history with pagination
     */
    public List<Loan> getLoanHistory(Long branchId, int page, int size) {
        return loanRepo.findLoanHistoryByBranch(branchId, page * size, size);
    }
}
