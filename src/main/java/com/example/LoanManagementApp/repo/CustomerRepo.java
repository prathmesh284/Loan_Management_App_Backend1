package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, String> {
	boolean existsByCustomerId(String customerId);
	List<Customer> findByBranchId(Long branchId);
    Optional<Customer> findByCustomerId(String customerId);
    void deleteByCustomerId(String customerId);
}