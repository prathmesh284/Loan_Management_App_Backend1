package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Document;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.repo.CustomerRepo;
import com.example.LoanManagementApp.repo.DocumentRepo;
import com.example.LoanManagementApp.service.S3Service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin("*")
@Slf4j
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
        log.info(
                "[DOC-UPLOAD] Request received: customerId={}, docType={}, docName={}, originalFilename={}, size={} bytes, contentType={}",
                customerId,
                docType,
                docName,
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : null,
                file != null ? file.getContentType() : null
        );

        log.info("[DOC-UPLOAD] Looking up customer by customerId={}", customerId);
        Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
        if (customerOpt.isEmpty()) {
            log.warn("[DOC-UPLOAD] Customer not found: {}", customerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + customerId);
        }

        log.info("[DOC-UPLOAD] Customer found: {}", customerOpt.get().getName());
        log.info("[DOC-UPLOAD] Starting S3 upload for customerId={}", customerId);
        String url = s3Service.uploadFile(file);
        log.info("[DOC-UPLOAD] S3 upload completed. URL={}", url);
        Customer customer = customerOpt.get();

        Document doc = new Document();
        doc.setCustomer(customer);
        doc.setDocType(docType);
        doc.setDocName((docName != null && !docName.isBlank()) ? docName : file.getOriginalFilename());
        doc.setS3Url(url);

        log.info("[DOC-UPLOAD] Saving document metadata to database for customerId={}", customerId);
        Document savedDocument = repo.save(doc);
        log.info("[DOC-UPLOAD] Document saved successfully with id={}", savedDocument.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedDocument));
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String keyword) {
        log.info("[DOC-SEARCH] Searching documents with keyword={}", keyword);
        List<Document> documents = repo.findByKeyword(keyword);
        return documents.stream().map(this::toResponse).toList();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getDocumentsByCustomer(@PathVariable String customerId) {
        log.info("[DOC-LIST] Fetching documents for customerId={}", customerId);
        Optional<Customer> customerOpt = customerRepo.findByCustomerId(customerId);
        if (customerOpt.isEmpty()) {
            log.warn("[DOC-LIST] Customer not found for document fetch: {}", customerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + customerId);
        }

        List<Document> documents = repo.findByCustomerPhone(customerId);
        log.info("[DOC-LIST] Found {} documents for customerId={}", documents.size(), customerId);
        return ResponseEntity.ok(documents.stream().map(this::toResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Optional<Document> documentOpt = repo.findById(id);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Document not found with ID: " + id);
        }

        Document document = documentOpt.get();
        String docName = body.get("docName");
        String docType = body.get("docType");

        if (docName != null && !docName.isBlank()) {
            document.setDocName(docName.trim());
        }
        if (docType != null && !docType.isBlank()) {
            document.setDocType(docType.trim());
        }

        Document savedDocument = repo.save(document);
        return ResponseEntity.ok(toResponse(savedDocument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        Optional<Document> documentOpt = repo.findById(id);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Document not found with ID: " + id);
        }

        Document document = documentOpt.get();
        s3Service.deleteFileFromUrl(document.getS3Url());
        repo.delete(document);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toResponse(Document document) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", document.getId());
        response.put("docType", document.getDocType());
        response.put("docName", document.getDocName());
        response.put("docDescription", document.getDocDescription());
        response.put("uploadDate", document.getUploadDate());
        response.put("isVerified", document.getIsVerified());
        response.put("s3Url", s3Service.generatePresignedUrlFromUrl(document.getS3Url(), 30));

        if (document.getCustomer() != null) {
            response.put("customerId", document.getCustomer().getCustomerId());
            response.put("customerName", document.getCustomer().getName());
        }

        return response;
    }
}
