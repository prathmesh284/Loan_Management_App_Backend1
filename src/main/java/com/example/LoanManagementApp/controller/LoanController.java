package com.example.LoanManagementApp.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.service.LoanRiskAssessmentService;
import com.example.LoanManagementApp.service.LoanService;
import com.example.LoanManagementApp.DTO.LoanRiskAssessmentResult;

@Slf4j
@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService service;
    
    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private LoanRiskAssessmentService loanRiskAssessmentService;

    // -------------------- CREATE LOAN --------------------
    @PostMapping("/add")
    public ResponseEntity<?> createLoan(@RequestBody Map<String, Object> request) {
        try {
            log.info("Received loan creation request for customerId={}", request.get("customerId"));

            // Extract customerId and load the Customer entity
            String customerId = (String) request.get("customerId");
            if (customerId == null || customerId.isEmpty()) {
                log.warn("Loan creation rejected because customerId is missing");
                return ResponseEntity.badRequest().body("Error: customerId is required");
            }
            
            Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
            if (customerOpt.isEmpty()) {
                log.warn("Loan creation rejected because customer was not found: {}", customerId);
                return ResponseEntity.badRequest().body("Error: Customer not found with ID: " + customerId);
            }
            
            // Create Loan from request
            Loan loan = new Loan();
            loan.setCustomer(customerOpt.get());
            loan.setGoldPurity(getString(request, "goldPurity"));
            loan.setGoldItemType(getString(request, "goldItemType", "goldType"));
            loan.setWeight(getDouble(request, "weight"));
            loan.setGoldPrice(getDouble(request, "goldPrice"));
            loan.setLtv(getDouble(request, "ltv"));
            loan.setInterestRate(getDouble(request, "interestRate"));
            loan.setTenure(getInteger(request, "tenure"));
            loan.setLoanAmount(getDouble(request, "loanAmount"));
            loan.setEmi(getDouble(request, "emi"));
            loan.setTotalInterest(getDouble(request, "totalInterest"));
            loan.setTotalAmount(getDouble(request, "totalAmount"));

            LoanRiskAssessmentResult riskAssessment = loanRiskAssessmentService.assessLoanApplication(
                    customerOpt.get(),
                    loan,
                    request
            );

            log.info(
                    "Loan risk assessment completed for customerId={} decision={} probability={}",
                    customerId,
                    riskAssessment.getRecommendedDecision(),
                    riskAssessment.getApprovalProbability()
            );

            if (!riskAssessment.isEligibleForAutoApproval()) {
                log.warn(
                        "Loan creation blocked by ML check for customerId={} decision={} probability={}",
                        customerId,
                        riskAssessment.getRecommendedDecision(),
                        riskAssessment.getApprovalProbability()
                );
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "success", false,
                        "message", riskAssessment.getMessage(),
                        "riskAssessment", riskAssessment
                ));
            }

            Loan saved = service.createLoan(loan);
            log.info("Loan created successfully with id={} for customerId={}", saved.getId(), customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Loan created successfully after ML risk validation.",
                    "loan", saved,
                    "riskAssessment", riskAssessment
            ));

        } catch (Exception e) {
            log.error("Loan creation failed for request={}", request, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error creating loan: " + e.getMessage()
            ));
        }
    }

    // -------------------- GET LOANS BY CUSTOMER --------------------
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Loan>> getLoansByCustomer(@PathVariable String customerId) {
        List<Loan> loans = service.getLoansByCustomer(customerId);
        return ResponseEntity.ok(loans);
    }

    // -------------------- GET ALL LOANS --------------------
    @GetMapping("/all")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(service.getAllLoans());
    }

    private String getString(Map<String, Object> request, String... keys) {
        for (String key : keys) {
            Object value = request.get(key);
            if (value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Double getDouble(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required numeric field: " + key);
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text.trim());
        }
        throw new IllegalArgumentException("Invalid numeric field: " + key + " value=" + value);
    }

    private Integer getInteger(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required integer field: " + key);
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        throw new IllegalArgumentException("Invalid integer field: " + key + " value=" + value);
    }
}
