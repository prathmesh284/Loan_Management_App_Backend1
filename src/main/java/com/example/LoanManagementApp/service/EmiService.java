package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.repo.EmiRepo;
import com.example.LoanManagementApp.repo.LoanRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmiService {

    @Autowired
    private EmiRepo emiRepository;

    @Autowired
    private LoanRepo loanRepository;

    public Emi payEmi(Long loanId, String method) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        int totalEmis = loan.getTotalEmis();
        int paidEmis = loan.getPaidEmis();

        if (paidEmis >= totalEmis) {
            throw new RuntimeException("Loan already completed");
        }

        double emiAmount = loan.getTotalAmount() / totalEmis;

        paidEmis += 1;
        int remainingEmis = totalEmis - paidEmis;

        double remainingAmount = loan.getTotalAmount() - emiAmount;

        // Update Loan table
        loan.setPaidEmis(paidEmis);
        loan.setTotalAmount(remainingAmount);
        loanRepository.save(loan);

        // Save EMI record
        Emi emi = new Emi(
                loan,
                emiAmount,
                remainingAmount,
                totalEmis,
                paidEmis,
                remainingEmis,
                method,
                LocalDate.now()
        );

        return emiRepository.save(emi);
    }

    public Emi payEmi(Emi request) {
        if (request.getAmountPaid() <= 0) {
            throw new RuntimeException("Amount paid must be greater than 0");
        }

        Loan loan = request.getLoan();
        if (loan == null) {
            throw new RuntimeException("Loan is required");
        }

        // Get current loan state
        int totalEmis = loan.getTotalEmis();
        int currentPaidEmis = loan.getPaidEmis();
        int remainingEmis = totalEmis - currentPaidEmis;

        // Calculate remaining amount
        double remainingAmount = loan.getRemainingAmount() - request.getAmountPaid();
        if (remainingAmount < 0) {
            remainingAmount = 0;
        }

        // Update loan with new payment info
        loan.setPaidEmis(currentPaidEmis + 1);
        loan.setRemainingEmis(remainingEmis - 1);
        loan.setRemainingAmount(remainingAmount);
        loanRepository.save(loan);

        // Create EMI record with all required fields
        Emi emi = new Emi();
        emi.setLoan(loan);
        emi.setAmountPaid(request.getAmountPaid());
        emi.setRemainingAmount(remainingAmount);
        emi.setTotalEmis(totalEmis);
        emi.setPaidEmis(currentPaidEmis + 1);
        emi.setRemainingEmis(remainingEmis - 1);
        emi.setPaymentMethod(request.getPaymentMethod());
        emi.setStatus("PAID");
        emi.setPaymentDate(LocalDate.now());

        return emiRepository.save(emi);
    }

    public List<Emi> getEmiHistory(Long loanId) {
        return emiRepository.findByLoanId(loanId);
    }
}