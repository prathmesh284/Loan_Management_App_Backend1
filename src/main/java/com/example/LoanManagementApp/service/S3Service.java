package com.example.LoanManagementApp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3;

    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), new ObjectMetadata());
        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }

        return amazonS3.getUrl(bucketName, fileName).toString();
    }
}