package com.example.LoanManagementApp.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.LoanManagementApp.model.Branch;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.repo.BranchRepo;
import com.example.LoanManagementApp.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService service;
    
    @Autowired
    private BranchRepo branchRepo;

    // ADD CUSTOMER (with branchId instead of full branch object)
    @PostMapping("/add")
    public ResponseEntity<?> addCustomer(@RequestBody Map<String, Object> request) {
        try {
            // Extract branchId and load the Branch entity
            Object branchIdObj = request.get("branchId");
            if (branchIdObj == null) {
                return ResponseEntity.badRequest().body("Error: branchId is required");
            }
            
            Long branchId = ((Number) branchIdObj).longValue();
            Optional<Branch> branchOpt = branchRepo.findById(branchId);
            
            if (branchOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Branch not found with ID: " + branchId);
            }
            
            // Create Customer from request
            Customer customer = new Customer();
            customer.setCustomerId((String) request.get("customerId"));
            customer.setName((String) request.get("name"));
            customer.setEmail((String) request.get("email"));
            customer.setAadharNumber((String) request.get("aadharNumber"));
            customer.setPanNumber((String) request.get("panNumber"));
            customer.setAddress((String) request.get("address"));
            customer.setBranch(branchOpt.get());
            
            Customer saved = service.addCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // UPDATE CUSTOMER
    @PutMapping("/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable String customerId, @RequestBody Customer updated) {
        try {
            return ResponseEntity.ok(service.updateCustomer(customerId, updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    // GET CUSTOMER BY ID
    @GetMapping("/by-id/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId) {
        Customer customer = service.getByCustomerId(customerId);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        return ResponseEntity.ok(customer);
    }

    // GET ALL CUSTOMERS
    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return service.getAllCustomers();
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Customer>> getCustomersByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(service.getCustomersByBranch(branchId));
    }

    // DELETE CUSTOMER
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable String customerId) {
        service.deleteByCustomerId(customerId);
    }
}
