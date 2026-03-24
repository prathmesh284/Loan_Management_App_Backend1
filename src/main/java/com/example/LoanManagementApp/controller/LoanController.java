package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.service.LoanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService service;

    // -------------------- CREATE LOAN --------------------
    @PostMapping("/add")
    public ResponseEntity<?> createLoan(@RequestBody Loan loan) {
        try {
            Loan saved = service.createLoan(loan);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating loan: " + e.getMessage());
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
