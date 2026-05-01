package com.example.LoanManagementApp.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.repo.CustomerRepo;

@Service
public class CustomerService {
	@Autowired
    private CustomerRepo repo;

    @Autowired
    private CustomerOtpService customerOtpService;
	 
	public Customer addCustomer(Customer customer) throws Exception {
		if (repo.existsById(customer.getCustomerId())) {
            throw new RuntimeException("Customer already exists with ID: " + customer.getCustomerId());
        }
        customer.setIsPhoneVerified(false);
        customer.setPhoneVerifiedAt(null);
        return repo.save(customer);
    }

    public java.util.Map<String, Object> sendVerificationOtp(Customer customer) {
        return customerOtpService.sendCustomerVerificationOtp(customer);
    }

    public java.util.Map<String, Object> verifyCustomerOtp(String customerId, String otpCode) {
        return customerOtpService.verifyCustomerOtp(customerId, otpCode);
    }

    public java.util.Map<String, Object> resendVerificationOtp(String customerId) {
        return customerOtpService.resendCustomerVerificationOtp(customerId);
    }

    public boolean verifyCustomerOtpForLoanCreation(String customerId, String otpCode) {
        return customerOtpService.verifyCustomerOtpForLoanCreation(customerId, otpCode);
    }
	
	public Customer updateCustomer(String customerId, Customer updated) {
        Customer existing = repo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        // update allowed fields
        existing.setName(updated.getName() != null ? updated.getName() : existing.getName());
        existing.setEmail(updated.getEmail() != null ? updated.getEmail() : existing.getEmail());
        existing.setAadharNumber(updated.getAadharNumber() != null ? updated.getAadharNumber() : existing.getAadharNumber());
        existing.setPanNumber(updated.getPanNumber() != null ? updated.getPanNumber() : existing.getPanNumber());
        existing.setAddress(updated.getAddress() != null ? updated.getAddress() : existing.getAddress());
        return repo.save(existing);
    }
	
	public Customer getByCustomerId(String customerId) {
        return repo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
	
	public List<Customer> getAllCustomers() {
        return repo.findAll();
    }
	
	public List<Customer> getCustomersByBranch(Long branchId) {
        return repo.findByBranchId(branchId);
    }
	
	public void deleteByCustomerId(String customerId) {
        if (!repo.existsByCustomerId(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        repo.deleteByCustomerId(customerId);
    }

}
