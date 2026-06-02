package com.example.LoanManagementApp.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;

@Service
public class TwilioNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TwilioNotificationService.class);

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
            log.warn("Twilio SMS skipped because twilioEnabled=false");
            return;
        }

        if (!isConfigured()) {
            throw new IllegalStateException("Twilio configuration is incomplete");
        }

        String toNumber = normalizeIndianPhoneNumber(phoneNumber);
        String fromNumberNormalized = normalizeTwilioFromNumber();

        if (toNumber.isBlank()) {
            throw new IllegalArgumentException("Invalid destination phone number for Twilio SMS: " + phoneNumber);
        }

        log.info("Sending Twilio SMS from={} to={} body={}...", fromNumberNormalized, toNumber,
                messageBody == null ? "<empty>" : messageBody.length() > 80 ? messageBody.substring(0, 80) + "..." : messageBody);

        int attempts = 0;
        int maxAttempts = 2;
        long backoffMs = 1000L;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(fromNumberNormalized),
                        messageBody
                ).create();
                // success
                return;
            } catch (ApiException e) {
                Throwable root = e.getCause() != null ? e.getCause() : e;
                boolean isConnectTimeout = root instanceof ConnectTimeoutException
                        || root instanceof SocketTimeoutException
                        || (e.getMessage() != null && e.getMessage().contains("Connect timed out"));

                if (isConnectTimeout) {
                    log.error("Twilio connect timeout when sending SMS from={} to={} (attempt {}/{}). Possible network egress/VPC NAT issue. Message={}",
                            fromNumberNormalized, toNumber, attempts, maxAttempts,
                            messageBody == null ? "<empty>" : (messageBody.length() > 120 ? messageBody.substring(0, 120) + "..." : messageBody), e);
                } else {
                    log.error("Twilio API rejected SMS from={} to={} errorCode={} status={} message={}",
                            fromNumberNormalized, toNumber, e.getCode(), e.getStatusCode(), e.getMessage(), e);
                }

                if (attempts >= maxAttempts) {
                    if (isConnectTimeout) {
                        throw new IllegalStateException("Twilio connect timeout: cannot reach api.twilio.com. Check Lambda VPC/NAT/firewall and Twilio API reachability.", e);
                    }
                    throw e;
                }
            } catch (Exception e) {
                log.error("Twilio SMS failed from={} to={} attempt {}/{} error={}", fromNumberNormalized, toNumber, attempts, maxAttempts, e.getMessage(), e);
                if (attempts >= maxAttempts) {
                    throw e;
                }
            }

            try {
                Thread.sleep(backoffMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
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
