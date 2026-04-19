package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepo extends JpaRepository<Receipt, Long> {

    // Find receipt by receipt number
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    // Find receipt by transaction ID
    Optional<Receipt> findByTransactionId(String transactionId);

    // Find receipts by EMI
    List<Receipt> findByEmiId(Long emiId);

    // Find receipts by Loan
    List<Receipt> findByLoanId(Long loanId);

    // Find receipts by Customer
    List<Receipt> findByCustomerCustomerId(String customerId);

    // Find receipts by status
    List<Receipt> findByStatus(String status);

    // Find pending receipts
    List<Receipt> findByStatusOrderByIssuedDateDesc(String status);

    // Find receipts in date range
    @Query("SELECT r FROM Receipt r WHERE r.issuedDate BETWEEN :startDate AND :endDate ORDER BY r.issuedDate DESC")
    List<Receipt> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find receipts by payment method
    List<Receipt> findByPaymentMethod(String paymentMethod);

    // Find receipts by payment mode
    List<Receipt> findByPaymentMode(String paymentMode);

    // Paginated search for receipts by loan
    Page<Receipt> findByLoanId(Long loanId, Pageable pageable);

    // Search receipts by multiple criteria
    @Query("SELECT r FROM Receipt r WHERE " +
           "(:customerId IS NULL OR r.customer.customerId = :customerId) AND " +
           "(:loanId IS NULL OR r.loan.id = :loanId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:paymentMethod IS NULL OR r.paymentMethod = :paymentMethod) AND " +
           "r.issuedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.issuedDate DESC")
    Page<Receipt> searchReceipts(
            @Param("customerId") String customerId,
            @Param("loanId") Long loanId,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Count receipts by status
    long countByStatus(String status);

    // Count receipts by payment method for a loan
    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.loan.id = :loanId AND r.paymentMethod = :paymentMethod")
    long countByLoanAndPaymentMethod(@Param("loanId") Long loanId, @Param("paymentMethod") String paymentMethod);

    // Find recent receipts for a customer
    @Query(value = "SELECT * FROM receipts WHERE customer_id = :customerId ORDER BY issued_date DESC LIMIT :limit", nativeQuery = true)
    List<Receipt> findRecentReceiptsByCustomer(@Param("customerId") String customerId, @Param("limit") int limit);

    // Check if receipt number exists
    boolean existsByReceiptNumber(String receiptNumber);
}
