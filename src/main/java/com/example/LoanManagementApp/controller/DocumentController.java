package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Document;
import com.example.LoanManagementApp.repo.DocumentRepo;
import com.example.LoanManagementApp.service.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
@CrossOrigin("*")
public class DocumentController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DocumentRepo repo;

    @PostMapping("/upload")
    public Document upload(
            @RequestParam String customerId,
            @RequestParam String docType,
            @RequestParam String docName,
            @RequestParam MultipartFile file
    ) {

        String url = s3Service.uploadFile(file);

        Document doc = new Document();
        doc.setDocType(docType);
        doc.setDocName(docName);
        doc.setS3Url(url);

        return repo.save(doc);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String keyword) {
        return repo.findByKeyword(keyword);
    }
}