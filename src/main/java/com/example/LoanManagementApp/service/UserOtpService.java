package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.DTO.UserSignupDTO;
import com.example.LoanManagementApp.model.UserOtpVerification;
import com.example.LoanManagementApp.model.Users;
import com.example.LoanManagementApp.repo.UserOtpVerificationRepo;
import com.example.LoanManagementApp.repo.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserOtpService {

    private static final String USER_VERIFICATION_PURPOSE = "USER_SIGNUP_VERIFICATION";
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserOtpVerificationRepo otpRepo;
    private final UserRepo userRepo;
    private final TwilioNotificationService twilioNotificationService;

    @Value("${otp.verification.enabled:true}")
    private boolean otpEnabled;

    @Value("${otp.verification.expiry-minutes:10}")
    private int otpExpiryMinutes;

    public UserOtpService(
            UserOtpVerificationRepo otpRepo,
            UserRepo userRepo,
            TwilioNotificationService twilioNotificationService
    ) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.twilioNotificationService = twilioNotificationService;
    }

    @Transactional
    public Map<String, Object> sendSignupOtp(Users user) {
        if (!otpEnabled) {
            user.setIsPhoneVerified(true);
            user.setPhoneVerifiedAt(LocalDateTime.now());
            userRepo.save(user);
            return Map.of(
                    "success", true,
                    "message", "User verification OTP is disabled",
                    "phoneVerified", true
            );
        }

        expireActiveOtps(user.getPhoneNumber());
        String otpCode = generateOtp();

        UserOtpVerification otpVerification = new UserOtpVerification();
        otpVerification.setPhoneNumber(user.getPhoneNumber());
        otpVerification.setPurpose(USER_VERIFICATION_PURPOSE);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        otpRepo.save(otpVerification);

        twilioNotificationService.sendSms(
                user.getPhoneNumber(),
                "Hello " + user.getUsername()
                        + ", your OTP for staff account verification is "
                        + otpCode
                        + ". It is valid for "
                        + otpExpiryMinutes
                        + " minutes."
        );

        return Map.of(
                "success", true,
                "message", "OTP sent to user phone number",
                "phoneNumber", user.getPhoneNumber(),
                "phoneVerified", false,
                "expiresInMinutes", otpExpiryMinutes
        );
    }

    @Transactional
    public Map<String, Object> verifySignupOtp(String phoneNumber, String otpCode) {
        Users user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new IllegalArgumentException("User not found with phone number: " + phoneNumber);
        }

        UserOtpVerification otpVerification = otpRepo
                .findTopByPhoneNumberAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                        phoneNumber,
                        USER_VERIFICATION_PURPOSE
                )
                .orElseThrow(() -> new IllegalArgumentException("No active OTP found for user"));

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

        user.setIsPhoneVerified(true);
        user.setPhoneVerifiedAt(LocalDateTime.now());
        userRepo.save(user);

        twilioNotificationService.sendSms(
                user.getPhoneNumber(),
                "Welcome to Gold Loan Management, " + user.getUsername()
                        + ". Your staff account is verified and ready to use."
        );

        return Map.of(
                "success", true,
                "message", "User verified successfully",
                "phoneNumber", phoneNumber,
                "phoneVerified", true
        );
    }

    @Transactional
    public Map<String, Object> resendSignupOtp(String phoneNumber) {
        Users user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new IllegalArgumentException("User not found with phone number: " + phoneNumber);
        }
        return sendSignupOtp(user);
    }

    private void expireActiveOtps(String phoneNumber) {
        List<UserOtpVerification> activeOtps = otpRepo.findByPhoneNumberAndPurposeAndConsumedFalse(
                phoneNumber,
                USER_VERIFICATION_PURPOSE
        );
        for (UserOtpVerification otp : activeOtps) {
            otp.setConsumed(true);
        }
        otpRepo.saveAll(activeOtps);
    }

    private String generateOtp() {
        int otpNumber = 100000 + OTP_RANDOM.nextInt(900000);
        return String.valueOf(otpNumber);
    }
}
