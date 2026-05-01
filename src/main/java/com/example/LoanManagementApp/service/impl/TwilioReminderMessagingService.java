package com.example.LoanManagementApp.service.impl;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.service.ReminderDispatchResult;
import com.example.LoanManagementApp.service.ReminderMessagingService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TwilioReminderMessagingService implements ReminderMessagingService {

    @Value("${reminder.twilio.enabled:true}")
    private boolean twilioEnabled;

    @Value("${reminder.twilio.account-sid:}")
    private String accountSid;

    @Value("${reminder.twilio.auth-token:}")
    private String authToken;

    @Value("${reminder.twilio.from-number:}")
    private String fromNumber;

    @Value("${reminder.twilio.channel:SMS}")
    private String configuredChannel;

    @PostConstruct
    void initializeTwilio() {
        if (isConfigured()) {
            Twilio.init(accountSid, authToken);
            if (isWhatsAppChannel()) {
                log.warn("Twilio WhatsApp is configured for free-form code-generated messages. "
                        + "These messages only work within WhatsApp's 24-hour customer service window.");
            }
        }
    }

    @Override
    public String getChannel() {
        return isWhatsAppChannel() ? "WHATSAPP" : "SMS";
    }

    @Override
    public ReminderDispatchResult sendReminder(Customer customer, Loan loan, String reminderType, String messageBody) {
        String channel = getChannel();

        if (!twilioEnabled) {
            return ReminderDispatchResult.failure(channel, "Twilio reminders are disabled");
        }

        if (!isConfigured()) {
            return ReminderDispatchResult.failure(channel, "Twilio configuration is incomplete");
        }

        if (isWhatsAppChannel() && !Boolean.TRUE.equals(customer.getIsWhatsappOptIn())) {
            return ReminderDispatchResult.failure(channel, "Customer has not opted in for WhatsApp reminders");
        }

        String toNumber = normalizeIndianPhoneNumber(customer.getCustomerId());
        if (toNumber.isBlank()) {
            return ReminderDispatchResult.failure(channel, "Customer phone number is invalid");
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(formatForChannel(toNumber)),
                    new PhoneNumber(formatForChannel(normalizeTwilioFromNumber())),
                    safeMessageBody(messageBody, customer, loan)
            ).create();

            return ReminderDispatchResult.success(channel, message.getSid());
        } catch (ApiException e) {
            log.error("Twilio API rejected reminder for customer={} loan={} channel={}",
                    customer.getCustomerId(), loan.getId(), channel, e);
            return ReminderDispatchResult.failure(channel, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send Twilio reminder for customer={} loan={} channel={}",
                    customer.getCustomerId(), loan.getId(), channel, e);
            return ReminderDispatchResult.failure(channel, e.getMessage());
        }
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
    }

    private boolean isWhatsAppChannel() {
        return "WHATSAPP".equalsIgnoreCase(configuredChannel);
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

    private String normalizeIndianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }

        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) {
            return "";
        }

        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        }

        if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
            return "+" + digitsOnly;
        }

        if (phoneNumber.startsWith("+")) {
            return "+" + digitsOnly;
        }

        return digitsOnly.startsWith("+") ? digitsOnly : "+" + digitsOnly;
    }

    private String formatForChannel(String phoneNumber) {
        if (isWhatsAppChannel()) {
            return phoneNumber.startsWith("whatsapp:") ? phoneNumber : "whatsapp:" + phoneNumber;
        }

        return phoneNumber;
    }

    private String safeMessageBody(String messageBody, Customer customer, Loan loan) {
        if (messageBody != null && !messageBody.isBlank()) {
            return messageBody;
        }

        return "Dear " + customer.getName()
                + ", your EMI payment is due on "
                + (loan.getNextEmiDate() == null ? "soon" : loan.getNextEmiDate())
                + ". Please complete the payment at the earliest.";
    }
}
