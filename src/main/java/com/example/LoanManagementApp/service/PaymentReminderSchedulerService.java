package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.model.PaymentReminderLog;
import com.example.LoanManagementApp.repo.PaymentReminderLogRepo;
import com.example.LoanManagementApp.repo.LoanRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentReminderSchedulerService {

    private final LoanRepo loanRepo;
    private final PaymentReminderLogRepo reminderLogRepo;
    private final PaymentGatewayService paymentGatewayService;
    private final ReminderMessagingService reminderMessagingService;

    @Value("${reminder.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${reminder.schedule.timezone:Asia/Kolkata}")
    private String reminderTimezone;

    public PaymentReminderSchedulerService(
            LoanRepo loanRepo,
            PaymentReminderLogRepo reminderLogRepo,
            PaymentGatewayService paymentGatewayService,
            ReminderMessagingService reminderMessagingService
    ) {
        this.loanRepo = loanRepo;
        this.reminderLogRepo = reminderLogRepo;
        this.paymentGatewayService = paymentGatewayService;
        this.reminderMessagingService = reminderMessagingService;
    }

    @Scheduled(cron = "0 0 10 * * *", zone = "${reminder.schedule.timezone:Asia/Kolkata}")
    public void sendMorningReminders() {
        runReminderDispatch("10AM");
    }

    @Scheduled(cron = "0 0 20 * * *", zone = "${reminder.schedule.timezone:Asia/Kolkata}")
    public void sendEveningReminders() {
        runReminderDispatch("8PM");
    }

    private void runReminderDispatch(String timeSlot) {
        if (!schedulerEnabled) {
            log.info("Payment reminder scheduler is disabled. Skipping {} reminder run.", timeSlot);
            return;
        }

        LocalDate today = LocalDate.now();
        Map<LocalDate, String> reminderTargets = new HashMap<>();
        reminderTargets.put(today.plusDays(3), "DUE_MINUS_3");
        reminderTargets.put(today.plusDays(2), "DUE_MINUS_2");
        reminderTargets.put(today, "DUE_TODAY");

        List<Loan> activeLoans = loanRepo.findByStatus("ACTIVE");
        log.info("Running payment reminders for {} in timezone {}. Active loans={}", timeSlot, reminderTimezone, activeLoans.size());

        for (Loan loan : activeLoans) {
            if (loan.getNextEmiDate() == null) {
                continue;
            }

            String reminderType = reminderTargets.get(loan.getNextEmiDate());
            if (reminderType == null) {
                continue;
            }

            dispatchReminder(loan, reminderType, timeSlot, today);
        }
    }

    private void dispatchReminder(Loan loan, String reminderType, String timeSlot, LocalDate reminderDate) {
        Customer customer = loan.getCustomer();
        if (customer == null) {
            return;
        }

        String channel = reminderMessagingService.getChannel();
        if (alreadySent(loan.getId(), reminderDate, reminderType, timeSlot, channel)) {
            log.info("Skipping duplicate reminder for loan={} type={} slot={}", loan.getId(), reminderType, timeSlot);
            return;
        }

        String paymentLink = null;
        try {
            paymentLink = paymentGatewayService.createPaymentLink(
                    calculateDueAmount(loan),
                    customer.getCustomerId(),
                    loan.getId(),
                    buildReminderOrderId(loan.getId(), reminderType, timeSlot),
                    buildReminderOptions(customer, reminderType)
            );
        } catch (Exception e) {
            log.warn("Could not create payment link for reminder loan={} type={} slot={}: {}",
                    loan.getId(), reminderType, timeSlot, e.getMessage());
        }

        String messageBody = buildReminderMessage(customer, loan, reminderType, paymentLink);
        ReminderDispatchResult result = reminderMessagingService.sendReminder(customer, loan, reminderType, messageBody);
        saveReminderLog(loan, customer, reminderDate, reminderType, timeSlot, result, paymentLink);
    }

    private boolean alreadySent(Long loanId, LocalDate reminderDate, String reminderType, String timeSlot, String channel) {
        return reminderLogRepo.existsByLoanIdAndReminderDateAndReminderTypeAndTimeSlotAndChannel(
                loanId, reminderDate, reminderType, timeSlot, channel
        );
    }

    private BigDecimal calculateDueAmount(Loan loan) {
        int remainingEmis = loan.getRemainingEmis() == null ? 0 : loan.getRemainingEmis();
        double remainingAmount = loan.getRemainingAmount() == null ? 0 : loan.getRemainingAmount();

        if (remainingEmis <= 0) {
            return BigDecimal.valueOf(Math.max(remainingAmount, 0)).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(remainingAmount)
                .divide(BigDecimal.valueOf(remainingEmis), 2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> buildReminderOptions(Customer customer, String reminderType) {
        Map<String, Object> options = new HashMap<>();
        options.put("paymentMethod", "UPI");
        options.put("sendToCustomer", true);
        options.put("receiptNumber", "AUTO_REMINDER_" + System.currentTimeMillis());
        options.put("notes", reminderType);

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("name", customer.getName());
        customerDetails.put("contact", customer.getCustomerId());
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            customerDetails.put("email", customer.getEmail());
        }
        options.put("customer", customerDetails);

        Map<String, Object> notify = new HashMap<>();
        notify.put("sms", true);
        notify.put("email", customer.getEmail() != null && !customer.getEmail().isBlank());
        options.put("notify", notify);
        return options;
    }

    private String buildReminderOrderId(Long loanId, String reminderType, String timeSlot) {
        return String.format("REM_%d_%s_%s_%d", loanId, reminderType, timeSlot, System.currentTimeMillis());
    }

    private String buildReminderMessage(Customer customer, Loan loan, String reminderType, String paymentLink) {
        String when = switch (reminderType) {
            case "DUE_MINUS_3" -> "in 3 days";
            case "DUE_MINUS_2" -> "in 2 days";
            case "DUE_TODAY" -> "today";
            default -> "soon";
        };

        String amount = calculateDueAmount(loan).setScale(2, RoundingMode.HALF_UP).toPlainString();
        StringBuilder builder = new StringBuilder()
                .append("Hello ").append(customer.getName())
                .append(", your EMI payment of Rs. ").append(amount)
                .append(" is due ").append(when)
                .append(" on ").append(loan.getNextEmiDate()).append(".");

        if (paymentLink != null && !paymentLink.isBlank()) {
            builder.append(" Please pay securely using this link: ").append(paymentLink);
        } else {
            builder.append(" Please contact the branch to complete your payment.");
        }

        builder.append(" Customer ID: ").append(customer.getCustomerId());
        return builder.toString();
    }

    private void saveReminderLog(
            Loan loan,
            Customer customer,
            LocalDate reminderDate,
            String reminderType,
            String timeSlot,
            ReminderDispatchResult result,
            String paymentLink
    ) {
        PaymentReminderLog logEntry = new PaymentReminderLog();
        logEntry.setLoan(loan);
        logEntry.setCustomer(customer);
        logEntry.setReminderDate(reminderDate);
        logEntry.setReminderType(reminderType);
        logEntry.setTimeSlot(timeSlot);
        logEntry.setChannel(result.getChannel());
        logEntry.setStatus(result.isSuccess() ? "SENT" : "FAILED");
        logEntry.setPaymentLink(paymentLink);
        logEntry.setProviderMessageId(result.getProviderMessageId());
        logEntry.setErrorMessage(result.getErrorMessage());
        reminderLogRepo.save(logEntry);
    }
}
