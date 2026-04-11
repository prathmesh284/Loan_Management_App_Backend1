package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepo extends JpaRepository<Loan, Long> {

    /**
     * Find all loans for a specific customer
     */
    List<Loan> findByCustomer(Customer customer);

    /**
     * Find loans by customer phone number (customerId)
     */
    @Query("SELECT l FROM Loan l WHERE l.customer.customerId = :customerId")
    List<Loan> findByCustomerId(@Param("customerId") String customerId);

    /**
     * Find active loans for a customer
     */
    @Query("SELECT l FROM Loan l WHERE l.customer.customerId = :customerId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoans(@Param("customerId") String customerId);

    /**
     * Find loans by branch
     */
    @Query("SELECT l FROM Loan l WHERE l.customer.branch.id = :branchId")
    List<Loan> findByBranchId(@Param("branchId") Long branchId);

    /**
     * Find loans by status
     */
    List<Loan> findByStatus(String status);

    /**
     * Count active loans for a customer
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.customer.customerId = :customerId AND l.status = 'ACTIVE'")
    long countActiveLoansByCustomerId(@Param("customerId") String customerId);

    /**
     * Find recent loans for a branch with limit
     */
    @Query(value = "SELECT * FROM loans WHERE customer_id IN " +
           "(SELECT customer_id FROM customers WHERE branch_id = :branchId) " +
           "ORDER BY loan_date DESC LIMIT :limit", nativeQuery = true)
    List<Loan> findRecentLoansByBranch(@Param("branchId") Long branchId, @Param("limit") int limit);

    /**
     * Find loan history with pagination
     */
    @Query(value = "SELECT * FROM loans WHERE customer_id IN " +
           "(SELECT customer_id FROM customers WHERE branch_id = :branchId) " +
           "ORDER BY loan_date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Loan> findLoanHistoryByBranch(@Param("branchId") Long branchId, 
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);
}
