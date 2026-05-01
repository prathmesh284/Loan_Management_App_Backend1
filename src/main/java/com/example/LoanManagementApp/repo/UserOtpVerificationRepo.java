package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.UserOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserOtpVerificationRepo extends JpaRepository<UserOtpVerification, Long> {

    List<UserOtpVerification> findByPhoneNumberAndPurposeAndConsumedFalse(String phoneNumber, String purpose);

    Optional<UserOtpVerification> findTopByPhoneNumberAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            String phoneNumber,
            String purpose
    );
}
