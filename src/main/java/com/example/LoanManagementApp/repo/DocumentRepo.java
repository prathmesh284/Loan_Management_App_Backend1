package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Document;
import com.example.LoanManagementApp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DocumentRepo extends JpaRepository<Document, Long> {

    /**
     * Find all documents for a specific customer
     */
    List<Document> findByCustomer(Customer customer);

    /**
     * Find documents by customer phone number (customerId)
     */
    @Query("SELECT d FROM Document d WHERE d.customer.customerId = :customerId")
    List<Document> findByCustomerPhone(@Param("customerId") String customerId);

    /**
     * Find documents by customer name or document name
     */
    @Query("SELECT d FROM Document d WHERE d.customer.name LIKE %:keyword% OR d.docName LIKE %:keyword%")
    List<Document> findByKeyword(@Param("keyword") String keyword);

    /**
     * Find documents by document type
     */
    List<Document> findByDocType(String docType);

    /**
     * Check if document exists for customer
     */
    boolean existsByCustomerAndDocType(Customer customer, String docType);
}