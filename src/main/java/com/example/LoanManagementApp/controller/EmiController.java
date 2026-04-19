package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.DTO.PaymentRequest;
import com.example.LoanManagementApp.DTO.PaymentResponse;
import com.example.LoanManagementApp.DTO.ReceiptResponse;
import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.model.Receipt;
import com.example.LoanManagementApp.repo.LoanRepo;
import com.example.LoanManagementApp.repo.ReceiptRepo;
import com.example.LoanManagementApp.service.EmiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced EMI Controller
 * Handles EMI payments, calculations, and receipt management
 */
@Slf4j
@RestController
@RequestMapping("/api/emis")
@CrossOrigin(origins = "*")
public class EmiController {

    @Autowired
    private EmiService emiService;

    @Autowired
    private LoanRepo loanRepository;

    @Autowired
    private ReceiptRepo receiptRepository;

    /**
     * Calculate monthly EMI for a loan
     * @param principal Loan principal amount
     * @param rate Annual interest rate
     * @param tenure Loan tenure in months
     * @return Monthly EMI amount
     */
    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateEmi(
            @RequestParam BigDecimal principal,
            @RequestParam BigDecimal rate,
            @RequestParam Integer tenure) {

        log.info("Calculating EMI for principal: {} rate: {} tenure: {}", principal, rate, tenure);

        try {
            BigDecimal monthlyEmi = emiService.calculateMonthlyEmi(principal, rate, tenure);
            BigDecimal totalAmount = monthlyEmi.multiply(BigDecimal.valueOf(tenure));
            BigDecimal totalInterest = totalAmount.subtract(principal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("principal", principal);
            response.put("rate", rate);
            response.put("tenure", tenure);
            response.put("monthlyEmi", monthlyEmi);
            response.put("totalAmount", totalAmount);
            response.put("totalInterest", totalInterest);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating EMI", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Process payment with comprehensive flow (Cash or Online)
     */
    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("Processing payment request: loanId={} amount={} method={}",
                paymentRequest.getLoanId(), paymentRequest.getAmount(), paymentRequest.getPaymentMethod());

        try {
            PaymentResponse response = emiService.processPayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .success(false)
                            .status("FAILED")
                            .message("Payment processing failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Pay EMI - Legacy endpoint (backward compatibility)
     */
    @PostMapping("/pay/{loanId}")
    public ResponseEntity<?> payEmi(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "CASH") String method) {
        log.info("Pay EMI request: loanId={} method={}", loanId, method);

        try {
            Emi emi = emiService.payEmi(loanId, method);
            return ResponseEntity.ok(emi);
        } catch (Exception e) {
            log.error("Error paying EMI", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get EMI history for a loan
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<List<Emi>> getEmiHistory(@PathVariable Long loanId) {
        log.info("Fetching EMI history for loan: {}", loanId);

        try {
            List<Emi> emiHistory = emiService.getEmiHistory(loanId);
            return ResponseEntity.ok(emiHistory);
        } catch (Exception e) {
            log.error("Error fetching EMI history", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    /**
     * Get loan payment schedule
     */
    @GetMapping("/schedule/{loanId}")
    public ResponseEntity<Map<String, Object>> getPaymentSchedule(@PathVariable Long loanId) {
        log.info("Fetching payment schedule for loan: {}", loanId);

        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found"));

            int remainingEmis = loan.getRemainingEmis();
            BigDecimal remainingAmount = BigDecimal.valueOf(loan.getRemainingAmount());
            BigDecimal monthlyEmi = remainingEmis > 0 ?
                    remainingAmount.divide(BigDecimal.valueOf(remainingEmis), 2, java.math.RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loanId", loanId);
            response.put("totalEmis", loan.getTotalEmis());
            response.put("paidEmis", loan.getPaidEmis());
            response.put("remainingEmis", remainingEmis);
            response.put("monthlyEmi", monthlyEmi);
            response.put("totalAmount", loan.getTotalAmount());
            response.put("remainingAmount", remainingAmount);
            response.put("nextEmiDate", loan.getNextEmiDate());
            response.put("status", loan.getStatus());

            // Generate schedule for next 12 months
            List<Map<String, Object>> schedule = generatePaymentSchedule(loan, monthlyEmi);
            response.put("upcomingPayments", schedule);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment schedule", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get receipt by receipt number
     */
    @GetMapping("/receipt/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable String receiptNumber) {
        log.info("Fetching receipt: {}", receiptNumber);

        try {
            Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                    .orElseThrow(() -> new RuntimeException("Receipt not found"));

            return ResponseEntity.ok(mapToReceiptResponse(receipt));
        } catch (Exception e) {
            log.error("Error fetching receipt", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get receipts for a loan
     */
    @GetMapping("/receipts/loan/{loanId}")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByLoan(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching receipts for loan: {}", loanId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("issuedDate").descending());
            List<Receipt> receipts = receiptRepository.findByLoanId(loanId);

            List<ReceiptResponse> responses = receipts.stream()
                    .map(this::mapToReceiptResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching loan receipts", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get receipts for a customer
     */
    @GetMapping("/receipts/customer/{customerId}")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByCustomer(
            @PathVariable String customerId) {

        log.info("Fetching receipts for customer: {}", customerId);

        try {
            List<Receipt> receipts = receiptRepository.findByCustomerCustomerId(customerId);

            List<ReceiptResponse> responses = receipts.stream()
                    .map(this::mapToReceiptResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching customer receipts", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Download receipt (text format)
     */
    @GetMapping("/receipt/{receiptNumber}/download")
    public ResponseEntity<String> downloadReceipt(@PathVariable String receiptNumber) {
        log.info("Downloading receipt: {}", receiptNumber);

        try {
            Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                    .orElseThrow(() -> new RuntimeException("Receipt not found"));

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + receiptNumber + ".txt\"")
                    .body("Receipt content would be generated here");
        } catch (Exception e) {
            log.error("Error downloading receipt", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get payment statistics for a loan
     */
    @GetMapping("/stats/{loanId}")
    public ResponseEntity<Map<String, Object>> getPaymentStats(@PathVariable Long loanId) {
        log.info("Fetching payment statistics for loan: {}", loanId);

        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found"));

            List<Receipt> receipts = receiptRepository.findByLoanId(loanId);

            Map<String, Long> paymentMethodCount = receipts.stream()
                    .collect(Collectors.groupingBy(Receipt::getPaymentMethod, Collectors.counting()));

            BigDecimal totalPaid = receipts.stream()
                    .map(Receipt::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> response = new HashMap<>();
            response.put("loanId", loanId);
            response.put("totalPayments", receipts.size());
            response.put("totalPaidAmount", totalPaid);
            response.put("paymentMethodBreakdown", paymentMethodCount);
            response.put("averagePaymentAmount", receipts.isEmpty() ? BigDecimal.ZERO :
                    totalPaid.divide(BigDecimal.valueOf(receipts.size()), 2, java.math.RoundingMode.HALF_UP));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment statistics", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Generate payment schedule
     */
    private List<Map<String, Object>> generatePaymentSchedule(Loan loan, BigDecimal monthlyEmi) {
        List<Map<String, Object>> schedule = new java.util.ArrayList<>();

        LocalDate currentDate = loan.getNextEmiDate() != null ? loan.getNextEmiDate() : LocalDate.now().plusMonths(1);
        int remainingEmis = loan.getRemainingEmis();

        for (int i = 0; i < Math.min(remainingEmis, 12); i++) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("emiNumber", loan.getPaidEmis() + i + 1);
            payment.put("dueDate", currentDate);
            payment.put("amount", monthlyEmi);
            payment.put("status", "PENDING");

            schedule.add(payment);
            currentDate = currentDate.plusMonths(1);
        }

        return schedule;
    }

    /**
     * Map Receipt to ReceiptResponse DTO
     */
    private ReceiptResponse mapToReceiptResponse(Receipt receipt) {
        ReceiptResponse response = new ReceiptResponse();
        response.setReceiptId(receipt.getId());
        response.setReceiptNumber(receipt.getReceiptNumber());
        response.setEmiId(receipt.getEmi().getId());
        response.setLoanId(receipt.getLoan().getId());
        response.setCustomerId(receipt.getCustomer().getCustomerId());
        response.setCustomerName(receipt.getCustomer().getName());
        response.setAmountPaid(receipt.getAmountPaid());
        response.setPaymentMethod(receipt.getPaymentMethod());
        response.setPaymentMode(receipt.getPaymentMode());
        response.setTransactionId(receipt.getTransactionId());
        response.setStatus(receipt.getStatus());
        response.setIssuedDate(receipt.getIssuedDate());
        response.setPaidDate(receipt.getPaidDate());
        response.setReceiptPath(receipt.getReceiptPath());
        response.setRemarks(receipt.getRemarks());

        // Loan details
        response.setLoanAmount(BigDecimal.valueOf(receipt.getLoan().getLoanAmount()));
        response.setTotalAmount(BigDecimal.valueOf(receipt.getLoan().getTotalAmount()));
        response.setTotalEmis(receipt.getLoan().getTotalEmis());
        response.setEmiTenure(receipt.getLoan().getTenure());
        response.setPaidEmis(receipt.getLoan().getPaidEmis());
        response.setRemainingEmis(receipt.getLoan().getRemainingEmis());
        response.setRemainingLoanAmount(BigDecimal.valueOf(receipt.getLoan().getRemainingAmount()));

        return response;
    }
}
