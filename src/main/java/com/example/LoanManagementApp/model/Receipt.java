package com.example.LoanManagementApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Receipt number cannot be null")
    @Column(unique = true, nullable = false)
    private String receiptNumber; // Format: RECIPT-YYYYMM_CUSTOMERID_LASTNUM_NO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_id", nullable = false)
    private Emi emi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Amount paid cannot be null")
    @Column(nullable = false)
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method cannot be null")
    @Column(nullable = false)
    private String paymentMethod; // CASH, UPI, CHEQUE, ONLINE_TRANSFER

    @Column(nullable = false)
    private String paymentMode; // MANUAL, RAZORPAY, PAYPAL, etc.

    @Column(unique = true)
    private String transactionId; // Gateway transaction ID (for online payments)

    @Column(unique = true)
    private String paymentLink; // Razorpay or other gateway payment link

    @NotNull(message = "Receipt status cannot be null")
    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, FAILED, CANCELLED

    @NotNull(message = "Receipt issued date cannot be null")
    @Column(nullable = false)
    private LocalDateTime issuedDate;

    @Column
    private LocalDateTime paidDate; // Date when payment was actually made

    @Column
    private String receiptPath; // Path to generated PDF receipt

    @Column
    private String remarks; // Additional remarks/notes

    // Constructor for creating receipt with auto-generated number
    public Receipt(Emi emi, Loan loan, Customer customer, BigDecimal amountPaid, 
                  String paymentMethod, String paymentMode) {
        this.emi = emi;
        this.loan = loan;
        this.customer = customer;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.paymentMode = paymentMode;
        this.status = "PENDING";
        this.issuedDate = LocalDateTime.now();
        this.receiptNumber = generateReceiptNumber(customer);
    }

    /**
     * Generates receipt number in format: RECIPT-YYYYMM_CUSTOMERID_LASTNUM_NO
     * Example: RECIPT-202604_CUST001_5_001
     */
    public static String generateReceiptNumber(Customer customer) {
        String pattern = "RECIPT-YYYYMM_CUSTOMERID_LASTNUM_NO";
        
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = String.format("%04d%02d", now.getYear(), now.getMonthValue());
        
        // Extract customer ID (e.g., "CUST001" -> "001")
        String customerId = customer.getCustomerId();
        String lastNum = customerId.replaceAll("[^0-9]", ""); // Extract numbers
        lastNum = lastNum.length() > 0 ? lastNum.substring(Math.max(0, lastNum.length() - 3)) : "000"; // Last 3 digits
        
        // Sequential number (will be handled by database or service)
        String sequenceNo = "001";
        
        return String.format("RECIPT-%s_%s_%s_%s", yearMonth, customerId, lastNum, sequenceNo);
    }

    /**
     * Validate receipt number format using regex
     */
    public static boolean isValidReceiptNumber(String receiptNumber) {
        String regex = "^RECIPT-\\d{6}_[A-Z0-9]+_\\d{1,3}_\\d{3,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(receiptNumber);
        return matcher.matches();
    }

    /**
     * Extract year and month from receipt number
     */
    public String getReceiptYearMonth() {
        Pattern pattern = Pattern.compile("RECIPT-(\\d{6})_");
        Matcher matcher = pattern.matcher(receiptNumber);
        if (matcher.find()) {
            String yearMonth = matcher.group(1);
            return yearMonth.substring(0, 4) + "-" + yearMonth.substring(4);
        }
        return null;
    }
}
