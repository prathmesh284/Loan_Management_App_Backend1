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
                loanId,
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

        Emi emi = new Emi();

        emi.setLoanId(request.getLoanId());
        emi.setAmountPaid(request.getAmountPaid());
        emi.setPaymentMethod(request.getPaymentMethod());
        emi.setStatus("PAID");
        emi.setPaymentDate(LocalDate.now());

        return emiRepository.save(emi); // 🔥 THIS LINE IS EVERYTHING
    }

    public List<Emi> getEmiHistory(Long loanId) {
        return emiRepository.findByLoanId(loanId);
    }
}