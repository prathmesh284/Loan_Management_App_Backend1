package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Branch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepo extends JpaRepository<Customer, String> {
    
    /**
     * Find customer by phone number (customerId)
     */
    Optional<Customer> findByCustomerId(String customerId);
    
    /**
     * Check if customer exists
     */
    boolean existsByCustomerId(String customerId);
    
    /**
     * Delete customer by phone number
     */
    void deleteByCustomerId(String customerId);
    
    /**
     * Find all customers in a specific branch
     */
    List<Customer> findByBranch(Branch branch);
    
    /**
     * Find all customers in a branch by branch id
     */
    @Query("SELECT c FROM Customer c WHERE c.branch.id = :branchId")
    List<Customer> findByBranchId(@Param("branchId") Long branchId);
    
    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customer by Aadhar number
     */
    Optional<Customer> findByAadharNumber(String aadharNumber);
    
    /**
     * Find customers by name (partial match)
     */
    List<Customer> findByNameContaining(String name);
}