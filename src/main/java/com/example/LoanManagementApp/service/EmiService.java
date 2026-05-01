package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.DTO.PaymentRequest;
import com.example.LoanManagementApp.DTO.PaymentResponse;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.model.Receipt;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.repo.EmiRepo;
import com.example.LoanManagementApp.repo.LoanRepo;
import com.example.LoanManagementApp.repo.ReceiptRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced EMI Service with payment gateway integration
 * Handles EMI calculations, payment processing, and receipt generation
 */
@Slf4j
@Service
public class EmiService {

    @Autowired
    private EmiRepo emiRepository;

    @Autowired
    private LoanRepo loanRepository;

    @Autowired
    private ReceiptRepo receiptRepository;

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired(required = false)
    private PaymentGatewayService paymentGatewayService;

    @Autowired(required = false)
    private ReceiptGenerationService receiptGenerationService;

    @Autowired
    private ReceiptNumberService receiptNumberService;

    /**
     * Calculate monthly EMI amount based on loan amount, tenure, and interest rate
     * Formula: EMI = P × [R(1+R)^N] / [(1+R)^N - 1]
     * Where: P = Principal, R = Monthly interest rate, N = Number of months
     */
    public BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be greater than 0");
        }
        if (tenureMonths == null || tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than 0");
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        BigDecimal numerator = monthlyRate.multiply(
                BigDecimal.ONE.add(monthlyRate).pow(tenureMonths)
        );

        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths).subtract(BigDecimal.ONE);

        BigDecimal emi = principal.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);

        log.info("Calculated EMI: {} for principal: {} at rate: {} for {} months",
                emi, principal, annualRate, tenureMonths);
        return emi;
    }

    /**
     * Process payment with comprehensive flow including receipt generation
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for loan: {} amount: {}", paymentRequest.getLoanId(), paymentRequest.getAmount());

        try {
            // Validate payment request
            if (!paymentRequest.isValidForPaymentType()) {
                return buildFailureResponse(paymentRequest.getLoanId(), null,
                        "VALIDATION_ERROR", "Missing required fields for payment type");
            }

            // Fetch loan
            Loan loan = loanRepository.findById(paymentRequest.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + paymentRequest.getLoanId()));

            // Fetch customer
            Customer customer = loan.getCustomer();
            if (customer == null) {
                throw new RuntimeException("Customer not found for loan");
            }

            // Validate payment amount
            BigDecimal remainingLoanAmount = BigDecimal.valueOf(loan.getRemainingAmount());
            if (paymentRequest.getAmount().compareTo(remainingLoanAmount) > 0) {
                return buildFailureResponse(paymentRequest.getLoanId(), null,
                        "AMOUNT_EXCEED_ERROR", "Payment amount exceeds remaining loan amount");
            }

            // Handle online payments - create payment link
            if (isOnlinePaymentMode(paymentRequest.getPaymentMode())) {
                if (paymentRequest.getPaymentId() == null || paymentRequest.getPaymentId().isEmpty()) {
                    // Generate payment link
                    String orderId = generateOrderId(paymentRequest.getLoanId(), customer);
                    String paymentLink = paymentGatewayService.createPaymentLink(
                            paymentRequest.getAmount(),
                            customer.getCustomerId(),
                            paymentRequest.getLoanId(),
                            orderId
                    );

                    return PaymentResponse.builder()
                            .success(false)
                            .status("PENDING")
                            .paymentLink(paymentLink)
                            .paymentMode(paymentRequest.getPaymentMode())
                            .loanId(paymentRequest.getLoanId())
                            .amountPaid(paymentRequest.getAmount())
                            .message("Payment link created. Please complete payment to confirm.")
                            .build();
                } else {
                    // Verify payment from gateway
                    boolean verified = paymentGatewayService.verifyPayment(
                            paymentRequest.getPaymentId(),
                            paymentRequest.getRazorpayOrderId()
                    );

                    if (!verified) {
                        return buildFailureResponse(paymentRequest.getLoanId(), null,
                                "PAYMENT_VERIFICATION_FAILED", "Payment could not be verified with gateway");
                    }
                }
            }

            // Create and save EMI record
            Emi emi = createEmiRecord(loan, paymentRequest);

            // Update loan details
            updateLoanAfterPayment(loan, paymentRequest);
            loanRepository.save(loan);

            // Generate receipt
            Receipt receipt = createReceipt(emi, loan, customer, paymentRequest);
            receiptRepository.save(receipt);

            // Generate PDF receipt if service available
            if (receiptGenerationService != null && paymentRequest.getGenerateReceipt()) {
                try {
                    String receiptPath = receiptGenerationService.generatePdfReceipt(receipt);
                    receipt.setReceiptPath(receiptPath);
                    receiptRepository.save(receipt);
                } catch (Exception e) {
                    log.warn("Could not generate PDF receipt: {}", e.getMessage());
                }
            }

            // Build success response
            return buildSuccessResponse(emi, loan, receipt, paymentRequest);

        } catch (Exception e) {
            log.error("Error processing payment", e);
            return buildFailureResponse(paymentRequest.getLoanId(), null,
                    "PAYMENT_FAILED", e.getMessage());
        }
    }

    /**
     * Create EMI record from payment request
     */
    private Emi createEmiRecord(Loan loan, PaymentRequest paymentRequest) {
        Emi emi = new Emi();
        emi.setLoan(loan);
        emi.setAmountPaid(paymentRequest.getAmount().doubleValue());
        
        Double newRemainingAmount = loan.getRemainingAmount() - paymentRequest.getAmount().doubleValue();
        emi.setRemainingAmount(newRemainingAmount);
        
        emi.setTotalEmis(loan.getTotalEmis());
        emi.setPaidEmis(loan.getPaidEmis() + 1);
        emi.setRemainingEmis(loan.getRemainingEmis() - 1);
        emi.setPaymentMethod(paymentRequest.getPaymentMethod());
        emi.setStatus("PAID");
        emi.setPaymentDate(LocalDate.now());

        return emiRepository.save(emi);
    }

    /**
     * Update loan details after payment
     */
    private void updateLoanAfterPayment(Loan loan, PaymentRequest paymentRequest) {
        Double paidAmount = loan.getPaidAmount() + paymentRequest.getAmount().doubleValue();
        Double remainingAmount = loan.getRemainingAmount() - paymentRequest.getAmount().doubleValue();
        
        loan.setPaidAmount(paidAmount);
        loan.setRemainingAmount(remainingAmount);
        loan.setPaidEmis(loan.getPaidEmis() + 1);
        loan.setRemainingEmis(loan.getRemainingEmis() - 1);

        // Set next EMI date
        LocalDate nextEmiDate = LocalDate.now().plusMonths(1);
        loan.setNextEmiDate(nextEmiDate);

        // Check if loan is fully paid
        if (remainingAmount <= 0) {
            loan.setStatus("CLOSED");
            log.info("Loan {} marked as CLOSED", loan.getId());
        }
    }

    /**
     * Create receipt record
     */
    private Receipt createReceipt(Emi emi, Loan loan, Customer customer, PaymentRequest paymentRequest) {
        Receipt receipt = new Receipt(emi, loan, customer, paymentRequest.getAmount(),
                paymentRequest.getPaymentMethod(), paymentRequest.getPaymentMode());
        receipt.setReceiptNumber(resolveReceiptNumber(customer, paymentRequest));

        receipt.setTransactionId(paymentRequest.getPaymentId());
        receipt.setStatus("CONFIRMED");
        receipt.setPaidDate(LocalDateTime.now());
        receipt.setRemarks(paymentRequest.getRemarks());

        return receipt;
    }

    /**
     * Check if payment mode is online
     */
    private boolean isOnlinePaymentMode(String paymentMode) {
        return paymentMode != null && (
                paymentMode.equals("RAZORPAY") ||
                paymentMode.equals("PAYPAL") ||
                paymentMode.equals("STRIPE") ||
                paymentMode.equals("BANK_TRANSFER")
        );
    }

    private String resolveReceiptNumber(Customer customer, PaymentRequest paymentRequest) {
        String requestedReceiptNumber = paymentRequest.getReceiptNumber();
        if (requestedReceiptNumber != null && !requestedReceiptNumber.isBlank()
                && !receiptRepository.existsByReceiptNumber(requestedReceiptNumber.trim())) {
            return requestedReceiptNumber.trim();
        }
        return receiptNumberService.generateReceiptNumber(customer);
    }

    /**
     * Generate unique order ID for payment
     */
    private String generateOrderId(Long loanId, Customer customer) {
        return String.format("ORD_%d_%s_%d", loanId, customer.getCustomerId(), System.currentTimeMillis());
    }

    /**
     * Build success response
     */
    private PaymentResponse buildSuccessResponse(Emi emi, Loan loan, Receipt receipt, PaymentRequest paymentRequest) {
        return PaymentResponse.builder()
                .success(true)
                .status("COMPLETED")
                .message("Payment processed successfully")
                .loanId(loan.getId())
                .emiId(emi.getId())
                .amountPaid(paymentRequest.getAmount())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .paymentMode(paymentRequest.getPaymentMode())
                .transactionId(paymentRequest.getPaymentId())
                .receiptId(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .receiptPath(receipt.getReceiptPath())
                .remainingAmount(BigDecimal.valueOf(loan.getRemainingAmount()))
                .paidEmis(loan.getPaidEmis())
                .remainingEmis(loan.getRemainingEmis())
                .emiStatus(emi.getStatus())
                .loanStatus(loan.getStatus())
                .totalRemainingAmount(BigDecimal.valueOf(loan.getRemainingAmount()))
                .nextEmiDate(loan.getNextEmiDate().atStartOfDay())
                .nextEmiAmount(calculateNextEmiAmount(loan))
                .paymentTime(LocalDateTime.now())
                .build();
    }

    /**
     * Build failure response
     */
    private PaymentResponse buildFailureResponse(Long loanId, Long emiId, String errorCode, String errorMessage) {
        return PaymentResponse.builder()
                .success(false)
                .status("FAILED")
                .message("Payment failed: " + errorMessage)
                .loanId(loanId)
                .emiId(emiId)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Calculate next EMI amount
     */
    private BigDecimal calculateNextEmiAmount(Loan loan) {
        if (loan.getRemainingEmis() <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(loan.getRemainingAmount()).divide(
                BigDecimal.valueOf(loan.getRemainingEmis()),
                2, RoundingMode.HALF_UP
        );
    }

    public Emi payEmi(Long loanId, String method) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        int totalEmis = loan.getTotalEmis();
        int paidEmis = loan.getPaidEmis();

        if (paidEmis >= totalEmis) {
            throw new RuntimeException("Loan already completed");
        }

        double emiAmount = loan.getTotalAmount() / totalEmis;

        paidEmis += 1;
        int remainingEmis = totalEmis - paidEmis;

        double remainingAmount = loan.getTotalAmount() - emiAmount;

        // Update Loan table
        loan.setPaidEmis(paidEmis);
        loan.setTotalAmount(remainingAmount);
        loanRepository.save(loan);

        // Save EMI record
        Emi emi = new Emi(
                loan,
                emiAmount,
                remainingAmount,
                totalEmis,
                paidEmis,
                remainingEmis,
                method,
                LocalDate.now()
        );

        return emiRepository.save(emi);
    }

    public Emi payEmi(Emi request) {
        if (request.getAmountPaid() <= 0) {
            throw new RuntimeException("Amount paid must be greater than 0");
        }

        Loan loan = request.getLoan();
        if (loan == null) {
            throw new RuntimeException("Loan is required");
        }

        // Get current loan state
        int totalEmis = loan.getTotalEmis();
        int currentPaidEmis = loan.getPaidEmis();
        int remainingEmis = totalEmis - currentPaidEmis;

        // Calculate remaining amount
        double remainingAmount = loan.getRemainingAmount() - request.getAmountPaid();
        if (remainingAmount < 0) {
            remainingAmount = 0;
        }

        // Update loan with new payment info
        loan.setPaidEmis(currentPaidEmis + 1);
        loan.setRemainingEmis(remainingEmis - 1);
        loan.setRemainingAmount(remainingAmount);
        loanRepository.save(loan);

        // Create EMI record with all required fields
        Emi emi = new Emi();
        emi.setLoan(loan);
        emi.setAmountPaid(request.getAmountPaid());
        emi.setRemainingAmount(remainingAmount);
        emi.setTotalEmis(totalEmis);
        emi.setPaidEmis(currentPaidEmis + 1);
        emi.setRemainingEmis(remainingEmis - 1);
        emi.setPaymentMethod(request.getPaymentMethod());
        emi.setStatus("PAID");
        emi.setPaymentDate(LocalDate.now());

        return emiRepository.save(emi);
    }

    public List<Emi> getEmiHistory(Long loanId) {
        return emiRepository.findByLoanId(loanId);
    }
}
