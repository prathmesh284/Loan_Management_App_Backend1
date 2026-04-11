package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * BranchRepository - Data access layer for Branch entity
 * Provides CRUD operations and custom queries for branch management
 */
@Repository
public interface BranchRepo extends JpaRepository<Branch, Long> {
    
    /**
     * Find branch by unique branch code
     */
    Optional<Branch> findByBranchCode(String branchCode);
    
    /**
     * Find all active branches
     */
    List<Branch> findByIsActiveTrue();
    
    /**
     * Find branches by city
     */
    List<Branch> findByCity(String city);
    
    /**
     * Find branches by state
     */
    List<Branch> findByState(String state);
    
    /**
     * Check if branch code exists
     */
    boolean existsByBranchCode(String branchCode);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if contact number exists
     */
    boolean existsByContactNumber(String contactNumber);

    /**
     * Find branch by branch name (used for user signup)
     */
    Optional<Branch> findByBranchName(String branchName);
}
