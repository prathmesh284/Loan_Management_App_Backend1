package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Document;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.repo.DocumentRepo;
import com.example.LoanManagementApp.service.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin("*")
public class DocumentController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DocumentRepo repo;

    @Autowired
    private CustomerRepo customerRepo;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam String customerId,
            @RequestParam String docType,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String docName,
            @RequestParam MultipartFile file
    ) {
        Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + customerId);
        }

        String url = s3Service.uploadFile(file);
        Customer customer = customerOpt.get();

        Document doc = new Document();
        doc.setCustomer(customer);
        doc.setDocType(docType);
        doc.setDocName((docName != null && !docName.isBlank()) ? docName : file.getOriginalFilename());
        doc.setS3Url(url);

        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(doc));
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String keyword) {
        return repo.findByKeyword(keyword);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getDocumentsByCustomer(@PathVariable String customerId) {
        Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + customerId);
        }

        return ResponseEntity.ok(repo.findByCustomerPhone(customerId));
    }
}
