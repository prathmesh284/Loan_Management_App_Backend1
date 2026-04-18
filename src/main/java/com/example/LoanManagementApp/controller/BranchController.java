package com.example.LoanManagementApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.LoanManagementApp.model.Branch;
import com.example.LoanManagementApp.repo.BranchRepo;
import java.util.List;
import java.util.Optional;

/**
 * BranchController - REST endpoints for branch operations
 * Handles branch retrieval and management
 */
@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "*")
public class BranchController {

    @Autowired
    private BranchRepo branchRepo;

    /**
     * Fetch all active branches
     * Used by signup page to populate branch dropdown
     * 
     * @return List of all active branches
     */
    @GetMapping
    public ResponseEntity<List<Branch>> getAllActiveBranches() {
        List<Branch> branches = branchRepo.findByIsActiveTrue();
        return ResponseEntity.ok(branches);
    }

    /**
     * Fetch branch by ID
     * Used by dashboard to display branch name
     * 
     * @param branchId Branch ID
     * @return Branch details
     */
    @GetMapping("/{branchId}")
    public ResponseEntity<?> getBranchById(@PathVariable Long branchId) {
        Optional<Branch> branch = branchRepo.findById(branchId);
        if (branch.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(branch.get());
    }
}
