package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepo extends JpaRepository<Document, Long> {

    List<Document> findByCustomerIdContainingOrCustomerNameContaining(
            String customerId,
            String customerName
    );
}