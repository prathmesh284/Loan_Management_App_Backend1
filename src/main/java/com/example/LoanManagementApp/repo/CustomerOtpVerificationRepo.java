package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.CustomerOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOtpVerificationRepo extends JpaRepository<CustomerOtpVerification, Long> {

    List<CustomerOtpVerification> findByCustomerIdAndPurposeAndConsumedFalse(String customerId, String purpose);

    Optional<CustomerOtpVerification> findTopByCustomerIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            String customerId,
            String purpose
    );
}
