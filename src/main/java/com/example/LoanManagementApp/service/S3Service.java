package com.example.LoanManagementApp.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * AWS S3 Service for file storage and retrieval
 * Handles receipt upload, download, and management
 */
@Slf4j
@Service
public class S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private AmazonS3 amazonS3;

    /**
     * Upload MultipartFile to S3
     * @param file File to upload
     * @return S3 URL of uploaded file
     */
    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            log.info(
                    "[S3-UPLOAD] Starting multipart upload to bucket={} key={} originalFilename={} size={} bytes contentType={}",
                    bucketName,
                    fileName,
                    file.getOriginalFilename(),
                    file.getSize(),
                    file.getContentType()
            );

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
            String url = amazonS3.getUrl(bucketName, fileName).toString();
            log.info("[S3-UPLOAD] File uploaded to S3 successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("[S3-UPLOAD] IO error uploading file to S3", e);
            throw new RuntimeException("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("[S3-UPLOAD] Unexpected error uploading file to S3", e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file to S3 bucket
     * @param filePath Local file path
     * @param s3Key S3 object key (path in bucket)
     * @return S3 URL of uploaded file
     */
    public String uploadFile(String filePath, String s3Key) {
        try {
            File file = new File(filePath);
            
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + filePath);
            }

            log.info("Uploading file to S3: {} -> s3://{}/{}", filePath, bucketName, s3Key);

            // Set metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            metadata.setContentType(getContentType(s3Key));
            java.util.Map<String, String> userMetadata = new java.util.HashMap<>();
            userMetadata.put("upload-date", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            metadata.setUserMetadata(userMetadata);

            // Upload file
            PutObjectRequest request = new PutObjectRequest(bucketName, s3Key, file);
            request.setMetadata(metadata);
            amazonS3.putObject(request);

            // Generate S3 URL
            String s3Url = amazonS3.getUrl(bucketName, s3Key).toString();
            log.info("File uploaded successfully: {}", s3Url);
            
            return s3Url;

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload file from input stream
     * @param inputStream File content stream
     * @param s3Key S3 object key
     * @param contentType File content type
     * @param contentLength File size
     * @return S3 URL
     */
    public String uploadFileFromStream(InputStream inputStream, String s3Key, String contentType, long contentLength) {
        try {
            log.info("Uploading file stream to S3: s3://{}/{}", bucketName, s3Key);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);

            PutObjectRequest request = new PutObjectRequest(bucketName, s3Key, inputStream, metadata);
            amazonS3.putObject(request);

            String s3Url = amazonS3.getUrl(bucketName, s3Key).toString();
            log.info("File stream uploaded successfully: {}", s3Url);
            
            return s3Url;

        } catch (Exception e) {
            log.error("Error uploading file stream to S3", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Download file from S3
     * @param s3Key S3 object key
     * @return File input stream
     */
    public InputStream downloadFile(String s3Key) {
        try {
            log.info("Downloading file from S3: s3://{}/{}", bucketName, s3Key);

            GetObjectRequest request = new GetObjectRequest(bucketName, s3Key);
            S3Object s3Object = amazonS3.getObject(request);

            return s3Object.getObjectContent();

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error downloading file: {}", e.getMessage());
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error downloading file from S3", e);
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    /**
     * Delete file from S3
     * @param s3Key S3 object key
     */
    public void deleteFile(String s3Key) {
        try {
            log.info("Deleting file from S3: s3://{}/{}", bucketName, s3Key);
            amazonS3.deleteObject(bucketName, s3Key);
            log.info("File deleted successfully");

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error deleting file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Check if file exists in S3
     * @param s3Key S3 object key
     * @return true if exists
     */
    public boolean fileExists(String s3Key) {
        try {
            return amazonS3.doesObjectExist(bucketName, s3Key);
        } catch (Exception e) {
            log.error("Error checking file existence in S3", e);
            return false;
        }
    }

    /**
     * Generate S3 URL for a key
     * @param s3Key S3 object key
     * @return S3 URL
     */
    public String generateS3Url(String s3Key) {
        return amazonS3.getUrl(bucketName, s3Key).toString();
    }

    /**
     * Generate presigned URL (time-limited access)
     * @param s3Key S3 object key
     * @param expirationMinutes Expiration time in minutes
     * @return Presigned URL
     */
    public String generatePresignedUrl(String s3Key, int expirationMinutes) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + (long) expirationMinutes * 60 * 1000);
            String url = amazonS3.generatePresignedUrl(bucketName, s3Key, expiration).toString();
            log.info("Generated presigned URL (expires in {} minutes)", expirationMinutes);
            return url;

        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage());
        }
    }

    /**
     * Determine content type based on file extension
     */
    private String getContentType(String s3Key) {
        if (s3Key.endsWith(".pdf")) {
            return "application/pdf";
        } else if (s3Key.endsWith(".html")) {
            return "text/html";
        } else if (s3Key.endsWith(".txt")) {
            return "text/plain";
        } else if (s3Key.endsWith(".json")) {
            return "application/json";
        }
        return "application/octet-stream";
    }

    /**
     * Get bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Get region
     */
    public String getRegion() {
        return region;
    }
}
