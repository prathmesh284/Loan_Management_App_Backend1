package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.repo.LoanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepo repo;

    public Loan createLoan(Loan loan) {
    	int tenure = Integer.parseInt(loan.getTenure()); // months
        loan.setTotalEmis(tenure);
        loan.setPaidEmis(0);
        loan.setRemainingEmis(tenure);
        loan.setPaidAmount(0.0);
        loan.setRemainingAmount(loan.getTotalAmount());  // totalAmount − paidAmount

        // Next EMI date = 1 month from loanDate OR today if not provided
        LocalDate nextDate = LocalDate.now().plusMonths(1);
        loan.setNextEmiDate(nextDate.toString());
        return repo.save(loan);
    }

    public List<Loan> getLoansByCustomer(String customerId) {
        return repo.findByCustomerId(customerId);
    }

    public List<Loan> getAllLoans() {
        return repo.findAll();
    }
}
