package com.example.LoanManagementApp.constants;

/**
 * ValidationConstants - Centralized validation patterns and messages
 * Reduces code duplication and maintains consistency across validators
 */
public class ValidationConstants {

    // ==================== REGEX PATTERNS ====================
    
    public static final String PHONE_PATTERN = "^[6-9]\\d{9}$";
    public static final String AADHAR_PATTERN = "^[0-9]{12}$";
    public static final String PAN_PATTERN = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String BRANCH_CODE_PATTERN = "^[A-Z]{3}[0-9]{3}$";
    public static final String GOLD_TYPE_PATTERN = "^(22K|23K|24K)$";
    public static final String PAYMENT_METHOD_PATTERN = "^(CASH|UPI|CHEQUE|ONLINE_TRANSFER)$";
    public static final String EMI_STATUS_PATTERN = "^(PENDING|PAID|OVERDUE|DEFAULT)$";
    public static final String LOAN_STATUS_PATTERN = "^(ACTIVE|CLOSED|DEFAULTED)$";
    public static final String DOCUMENT_TYPE_PATTERN = "^(KYC|LOAN|GOLD|PROPERTY|IDENTITY|ADDRESS|INCOME)$";

    // ==================== VALIDATION MESSAGES ====================
    
    // Phone
    public static final String PHONE_REQUIRED = "Phone number cannot be empty";
    public static final String PHONE_INVALID = "Phone number must be a valid 10-digit Indian number (6-9 followed by 9 digits)";

    // Email
    public static final String EMAIL_REQUIRED = "Email cannot be empty";
    public static final String EMAIL_INVALID = "Email must be valid (e.g., user@example.com)";

    // Aadhar
    public static final String AADHAR_REQUIRED = "Aadhar number cannot be empty";
    public static final String AADHAR_INVALID = "Aadhar number must be exactly 12 digits";

    // PAN
    public static final String PAN_REQUIRED = "PAN number cannot be empty";
    public static final String PAN_INVALID = "PAN format must be: 5 uppercase letters + 4 digits + 1 uppercase letter (e.g., AAAAA0000A)";

    // Name
    public static final String NAME_REQUIRED = "Name cannot be empty";
    public static final String NAME_LENGTH = "Name must be between 3 and 100 characters";
    public static final String NAME_INVALID = "Name can only contain letters and spaces";

    // Address
    public static final String ADDRESS_REQUIRED = "Address cannot be empty";
    public static final String ADDRESS_LENGTH = "Address must be between 10 and 255 characters";

    // Branch
    public static final String BRANCH_REQUIRED = "Branch cannot be null";
    public static final String BRANCH_CODE_REQUIRED = "Branch code cannot be empty";
    public static final String BRANCH_CODE_INVALID = "Branch code must follow format: 3 uppercase letters + 3 digits (e.g., MUM001)";

    // Gold Loan
    public static final String GOLD_TYPE_REQUIRED = "Gold type cannot be empty";
    public static final String GOLD_TYPE_INVALID = "Gold type must be 22K, 23K, or 24K";
    
    public static final String WEIGHT_REQUIRED = "Weight cannot be null";
    public static final String WEIGHT_INVALID = "Weight must be between 0.1 and 1000 grams";

    public static final String GOLD_PRICE_REQUIRED = "Gold price cannot be null";
    public static final String GOLD_PRICE_INVALID = "Gold price must be greater than 0";

    public static final String LTV_REQUIRED = "LTV (Loan-to-Value) cannot be null";
    public static final String LTV_INVALID = "LTV must be between 1 and 100 percent";

    public static final String INTEREST_RATE_REQUIRED = "Interest rate cannot be null";
    public static final String INTEREST_RATE_INVALID = "Interest rate must be between 0 and 100 percent";

    public static final String TENURE_REQUIRED = "Tenure cannot be null";
    public static final String TENURE_INVALID = "Tenure must be between 1 and 84 months";

    public static final String LOAN_AMOUNT_REQUIRED = "Loan amount cannot be null";
    public static final String LOAN_AMOUNT_INVALID = "Loan amount must be at least 1000 rupees";

    // EMI
    public static final String PAYMENT_METHOD_REQUIRED = "Payment method cannot be empty";
    public static final String PAYMENT_METHOD_INVALID = "Payment method must be CASH, UPI, CHEQUE, or ONLINE_TRANSFER";

    public static final String EMI_STATUS_REQUIRED = "EMI status cannot be empty";
    public static final String EMI_STATUS_INVALID = "EMI status must be PENDING, PAID, OVERDUE, or DEFAULT";

    public static final String PAYMENT_DATE_REQUIRED = "Payment date cannot be null";

    // Document
    public static final String DOCUMENT_TYPE_REQUIRED = "Document type cannot be empty";
    public static final String DOCUMENT_TYPE_INVALID = "Document type must be KYC, LOAN, GOLD, PROPERTY, IDENTITY, ADDRESS, or INCOME";

    public static final String DOCUMENT_NAME_REQUIRED = "Document name cannot be empty";
    public static final String DOCUMENT_NAME_LENGTH = "Document name must be between 3 and 100 characters";

    // Customer
    public static final String CUSTOMER_REQUIRED = "Customer cannot be null";
    public static final String CUSTOMER_ID_REQUIRED = "Customer ID (phone) cannot be empty";

    // User
    public static final String USERNAME_REQUIRED = "Username cannot be empty";
    public static final String USERNAME_LENGTH = "Username must be between 4 and 50 characters";
    public static final String USERNAME_INVALID = "Username can only contain letters, numbers, and underscores";

    public static final String PASSWORD_REQUIRED = "Password cannot be empty";
    public static final String PASSWORD_LENGTH = "Password must be at least 8 characters";

    public static final String GENDER_REQUIRED = "Gender cannot be null";
    public static final String GENDER_INVALID = "Gender must be MALE, FEMALE, or OTHER";

    // ==================== NUMERIC CONSTRAINTS ====================
    
    public static final double MIN_WEIGHT = 0.1;
    public static final double MAX_WEIGHT = 1000.0;
    
    public static final double MIN_LOAN_AMOUNT = 1000.0;
    
    public static final double MIN_LTV = 1.0;
    public static final double MAX_LTV = 100.0;
    
    public static final double MIN_INTEREST_RATE = 0.0;
    public static final double MAX_INTEREST_RATE = 100.0;
    
    public static final int MIN_TENURE = 1;
    public static final int MAX_TENURE = 84;
    
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 100;
    
    public static final int MIN_ADDRESS_LENGTH = 10;
    public static final int MAX_ADDRESS_LENGTH = 255;
    
    public static final int AADHAR_LENGTH = 12;
    public static final int PHONE_LENGTH = 10;

    // ==================== PRIVACY CONSTANTS ====================
    
    public static final String PASSWORD_ENCODING_PATTERN = "bcrypt";
}
