package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Emi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmiRepo extends JpaRepository<Emi, Long> {

    List<Emi> findByLoanId(Long loanId);
}