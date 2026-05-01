package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.CustomerOtpVerification;
import com.example.LoanManagementApp.repo.CustomerOtpVerificationRepo;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
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

    @Value("${otp.verification.enabled:true}")
    private boolean otpEnabled;

    @Value("${otp.verification.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${reminder.twilio.account-sid:}")
    private String accountSid;

    @Value("${reminder.twilio.auth-token:}")
    private String authToken;

    @Value("${reminder.twilio.from-number:}")
    private String fromNumber;

    public CustomerOtpService(CustomerOtpVerificationRepo otpRepo, CustomerRepo customerRepo) {
        this.otpRepo = otpRepo;
        this.customerRepo = customerRepo;
    }

    @PostConstruct
    void initializeTwilio() {
        if (isTwilioConfigured()) {
            Twilio.init(accountSid, authToken);
        }
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

        sendSms(customer.getCustomerId(), buildOtpMessage(customer.getName(), otpCode));

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
        if (otpCode == null || otpCode.isBlank()) {
            return false;
        }

        Map<String, Object> result = verifyCustomerOtp(customerId, otpCode.trim());
        return Boolean.TRUE.equals(result.get("success"));
    }

    private void expireActiveOtps(String customerId, String purpose) {
        List<CustomerOtpVerification> activeOtps = otpRepo.findByCustomerIdAndPurposeAndConsumedFalse(customerId, purpose);
        for (CustomerOtpVerification otp : activeOtps) {
            otp.setConsumed(true);
        }
        otpRepo.saveAll(activeOtps);
    }

    private void sendSms(String customerId, String messageBody) {
        if (!otpEnabled) {
            return;
        }

        if (!isTwilioConfigured()) {
            throw new IllegalStateException("Twilio OTP configuration is incomplete");
        }

        String toNumber = normalizeIndianPhoneNumber(customerId);
        String from = normalizeTwilioFromNumber();
        Message.creator(new PhoneNumber(toNumber), new PhoneNumber(from), messageBody).create();
    }

    private boolean isTwilioConfigured() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
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

    private String normalizeIndianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }

        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        }
        if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
            return "+" + digitsOnly;
        }
        if (phoneNumber.startsWith("+")) {
            return "+" + digitsOnly;
        }
        return "+" + digitsOnly;
    }

    private String normalizeTwilioFromNumber() {
        String trimmed = fromNumber == null ? "" : fromNumber.trim();
        if (trimmed.startsWith("whatsapp:")) {
            trimmed = trimmed.substring("whatsapp:".length());
        }

        String digitsOnly = trimmed.replaceAll("[^0-9+]", "");
        if (digitsOnly.startsWith("+")) {
            return digitsOnly;
        }
        if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
            return "+" + digitsOnly;
        }
        return digitsOnly;
    }
}
