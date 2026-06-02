package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.CustomerOtpVerification;
import com.example.LoanManagementApp.repo.CustomerOtpVerificationRepo;
import com.example.LoanManagementApp.repo.CustomerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomerOtpService {

    private static final String CUSTOMER_VERIFICATION_PURPOSE = "CUSTOMER_VERIFICATION";
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final CustomerOtpVerificationRepo otpRepo;
    private final CustomerRepo customerRepo;
    private final TwilioNotificationService twilioNotificationService;

    @Value("${otp.verification.enabled:true}")
    private boolean otpEnabled;

    @Value("${otp.verification.expiry-minutes:10}")
    private int otpExpiryMinutes;

    public CustomerOtpService(
            CustomerOtpVerificationRepo otpRepo,
            CustomerRepo customerRepo,
            TwilioNotificationService twilioNotificationService
    ) {
        this.otpRepo = otpRepo;
        this.customerRepo = customerRepo;
        this.twilioNotificationService = twilioNotificationService;
    }

    @Transactional
    public Map<String, Object> sendCustomerVerificationOtp(Customer customer) {
        if (!otpEnabled) {
            return Map.of(
                    "success", true,
                    "message", "OTP verification is disabled",
                    "phoneVerified", Boolean.TRUE.equals(customer.getIsPhoneVerified())
            );
        }

        expireActiveOtps(customer.getCustomerId(), CUSTOMER_VERIFICATION_PURPOSE);

        String otpCode = generateOtp();
        CustomerOtpVerification otpVerification = new CustomerOtpVerification();
        otpVerification.setCustomerId(customer.getCustomerId());
        otpVerification.setPurpose(CUSTOMER_VERIFICATION_PURPOSE);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        otpRepo.save(otpVerification);

        try {
            twilioNotificationService.sendSms(customer.getCustomerId(), buildOtpMessage(customer.getName(), otpCode));
        } catch (Exception e) {
            log.error("Failed to send customer OTP for customerId={}", customer.getCustomerId(), e);
            throw e;
        }

        return Map.of(
                "success", true,
                "message", "OTP sent successfully to customer phone number",
                "customerId", customer.getCustomerId(),
                "phoneVerified", false,
                "expiresInMinutes", otpExpiryMinutes
        );
    }

    @Transactional
    public Map<String, Object> verifyCustomerOtp(String customerId, String otpCode) {
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        CustomerOtpVerification otpVerification = otpRepo
                .findTopByCustomerIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                        customerId,
                        CUSTOMER_VERIFICATION_PURPOSE
                )
                .orElseThrow(() -> new IllegalArgumentException("No active OTP found for customer"));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpVerification.setConsumed(true);
            otpRepo.save(otpVerification);
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP");
        }

        if (!otpVerification.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpVerification.setConsumed(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpRepo.save(otpVerification);

        customer.setIsPhoneVerified(true);
        customer.setPhoneVerifiedAt(LocalDateTime.now());
        customerRepo.save(customer);

        twilioNotificationService.sendSms(
                customer.getCustomerId(),
                "Welcome " + customer.getName()
                        + ". Your customer profile has been verified successfully with Gold Loan Management."
        );

        return Map.of(
                "success", true,
                "message", "Customer phone verified successfully",
                "customerId", customerId,
                "phoneVerified", true
        );
    }

    @Transactional
    public Map<String, Object> resendCustomerVerificationOtp(String customerId) {
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
        return sendCustomerVerificationOtp(customer);
    }

    public boolean verifyCustomerOtpForLoanCreation(String customerId, String otpCode) {
        try {
            if (otpCode == null || otpCode.isBlank()) {
                return false;
            }

            Map<String, Object> result = verifyCustomerOtp(customerId, otpCode.trim());
            return Boolean.TRUE.equals(result.get("success"));
        } catch (Exception e) {
            log.warn("Customer OTP verification failed for loan creation customerId={}", customerId, e);
            return false;
        }
    }

    private void expireActiveOtps(String customerId, String purpose) {
        List<CustomerOtpVerification> activeOtps = otpRepo.findByCustomerIdAndPurposeAndConsumedFalse(customerId, purpose);
        for (CustomerOtpVerification otp : activeOtps) {
            otp.setConsumed(true);
        }
        otpRepo.saveAll(activeOtps);
    }

    private String buildOtpMessage(String customerName, String otpCode) {
        return "Hello " + (customerName == null ? "Customer" : customerName)
                + ", your OTP for customer verification is "
                + otpCode
                + ". It is valid for "
                + otpExpiryMinutes
                + " minutes. Do not share this OTP with anyone.";
    }

    private String generateOtp() {
        int otpNumber = 100000 + OTP_RANDOM.nextInt(900000);
        return String.valueOf(otpNumber);
    }
}
