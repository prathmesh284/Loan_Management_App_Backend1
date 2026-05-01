package com.example.LoanManagementApp.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioNotificationService {

    @Value("${reminder.twilio.enabled:true}")
    private boolean twilioEnabled;

    @Value("${reminder.twilio.account-sid:}")
    private String accountSid;

    @Value("${reminder.twilio.auth-token:}")
    private String authToken;

    @Value("${reminder.twilio.from-number:}")
    private String fromNumber;

    @PostConstruct
    void initializeTwilio() {
        if (isConfigured()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void sendSms(String phoneNumber, String messageBody) {
        if (!twilioEnabled) {
            return;
        }

        if (!isConfigured()) {
            throw new IllegalStateException("Twilio configuration is incomplete");
        }

        Message.creator(
                new PhoneNumber(normalizeIndianPhoneNumber(phoneNumber)),
                new PhoneNumber(normalizeTwilioFromNumber()),
                messageBody
        ).create();
    }

    public boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
    }

    public String normalizeIndianPhoneNumber(String phoneNumber) {
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
