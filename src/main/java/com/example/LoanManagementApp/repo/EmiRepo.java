package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmiRepo extends JpaRepository<Emi, Long> {

    /**
     * Find all EMI records for a specific loan
     */
    List<Emi> findByLoan(Loan loan);

    /**
     * Find all EMI records for a loan by loan ID
     */
    @Query("SELECT e FROM Emi e WHERE e.loan.id = :loanId")
    List<Emi> findByLoanId(@Param("loanId") Long loanId);

    /**
     * Find all pending EMI payments for a loan
     */
    @Query("SELECT e FROM Emi e WHERE e.loan.id = :loanId AND e.status = 'PENDING'")
    List<Emi> findPendingEmis(@Param("loanId") Long loanId);

    /**
     * Find all paid EMI records for a customer
     */
    @Query("SELECT e FROM Emi e WHERE e.loan.customer.customerId = :customerId AND e.status = 'PAID'")
    List<Emi> findPaidEmisForCustomer(@Param("customerId") String customerId);

    /**
     * Find EMI records by status
     */
    List<Emi> findByStatus(String status);

    /**
     * Count paid EMIs for a loan
     */
    @Query("SELECT COUNT(e) FROM Emi e WHERE e.loan.id = :loanId AND e.status = 'PAID'")
    long countPaidEmis(@Param("loanId") Long loanId);

    /**
     * Count overdue EMI payments
     */
    @Query("SELECT COUNT(e) FROM Emi e WHERE e.loan.id = :loanId AND e.status = 'OVERDUE'")
    long countOverdueEmis(@Param("loanId") Long loanId);

    /**
     * Find all pending EMIs across all loans
     */
    @Query("SELECT e FROM Emi e WHERE e.status = 'PENDING'")
    List<Emi> findPendingEmis();

    /**
     * Find all overdue EMIs before a specific date
     */
    @Query("SELECT e FROM Emi e WHERE e.paymentDate < :dueDate AND e.status IN ('PENDING', 'OVERDUE')")
    List<Emi> findOverdueEmis(@Param("dueDate") LocalDate dueDate);
}