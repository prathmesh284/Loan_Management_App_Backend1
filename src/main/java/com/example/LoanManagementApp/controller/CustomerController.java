package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService service;

    // ADD CUSTOMER (pure JSON)
    @PostMapping("/add")
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer) {
        try {
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
