package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Loan;

public interface ReminderMessagingService {

    String getChannel();

    ReminderDispatchResult sendReminder(Customer customer, Loan loan, String reminderType, String messageBody);
}
