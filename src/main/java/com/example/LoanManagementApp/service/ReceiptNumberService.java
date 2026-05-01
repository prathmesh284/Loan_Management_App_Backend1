package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.repo.ReceiptRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReceiptNumberService {

    private final ReceiptRepo receiptRepo;

    public ReceiptNumberService(ReceiptRepo receiptRepo) {
        this.receiptRepo = receiptRepo;
    }

    public String generateReceiptNumber(Customer customer) {
        if (customer == null || customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Customer is required to generate receipt number");
        }

        String customerId = customer.getCustomerId().trim();
        String yearMonth = currentYearMonth();
        String lastDigits = extractLastDigits(customerId);

        int sequence = 1;
        while (true) {
            String candidate = String.format(
                    "RECIPT-%s_%s_%s_%03d",
                    yearMonth,
                    customerId,
                    lastDigits,
                    sequence
            );

            if (!receiptRepo.existsByReceiptNumber(candidate)) {
                return candidate;
            }
            sequence++;
        }
    }

    private String currentYearMonth() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }

    private String extractLastDigits(String customerId) {
        String digitsOnly = customerId.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return "000";
        }
        return digitsOnly.substring(Math.max(0, digitsOnly.length() - 3));
    }
}
