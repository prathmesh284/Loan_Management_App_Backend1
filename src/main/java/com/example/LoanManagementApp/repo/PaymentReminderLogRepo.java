package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.PaymentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PaymentReminderLogRepo extends JpaRepository<PaymentReminderLog, Long> {

    boolean existsByLoanIdAndReminderDateAndReminderTypeAndTimeSlotAndChannel(
            Long loanId,
            LocalDate reminderDate,
            String reminderType,
            String timeSlot,
            String channel
    );
}
