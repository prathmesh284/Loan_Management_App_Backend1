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

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.service.LoanRiskAssessmentService;
import com.example.LoanManagementApp.service.LoanService;
import com.example.LoanManagementApp.DTO.LoanRiskAssessmentResult;

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
            // Extract customerId and load the Customer entity
            String customerId = (String) request.get("customerId");
            if (customerId == null || customerId.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: customerId is required");
            }
            
            Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Customer not found with ID: " + customerId);
            }
            
            // Create Loan from request
            Loan loan = new Loan();
            loan.setCustomer(customerOpt.get());
            loan.setGoldPurity((String) request.get("goldPurity"));
            loan.setGoldItemType((String) request.get("goldItemType"));
            loan.setWeight(((Number) request.get("weight")).doubleValue());
            loan.setGoldPrice(((Number) request.get("goldPrice")).doubleValue());
            loan.setLtv(((Number) request.get("ltv")).doubleValue());
            loan.setInterestRate(((Number) request.get("interestRate")).doubleValue());
            loan.setTenure(((Number) request.get("tenure")).intValue());
            loan.setLoanAmount(((Number) request.get("loanAmount")).doubleValue());
            loan.setEmi(((Number) request.get("emi")).doubleValue());
            loan.setTotalInterest(((Number) request.get("totalInterest")).doubleValue());
            loan.setTotalAmount(((Number) request.get("totalAmount")).doubleValue());

            LoanRiskAssessmentResult riskAssessment = loanRiskAssessmentService.assessLoanApplication(
                    customerOpt.get(),
                    loan,
                    request
            );

            if (!riskAssessment.isEligibleForAutoApproval()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "success", false,
                        "message", riskAssessment.getMessage(),
                        "riskAssessment", riskAssessment
                ));
            }

            Loan saved = service.createLoan(loan);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Loan created successfully after ML risk validation.",
                    "loan", saved,
                    "riskAssessment", riskAssessment
            ));

        } catch (Exception e) {
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
}
