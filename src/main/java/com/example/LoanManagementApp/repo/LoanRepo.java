package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepo extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(String customerId);
}
