package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.model.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Receipt Generation Service
 * Generates PDF receipts for both cash and online payments
 * Stores receipts in AWS S3 bucket
 */
@Slf4j
@Service
public class ReceiptGenerationService {



    @Autowired
    private S3Service s3Service;

    /**
     * Generate PDF receipt for payment and store in S3
     */
    public String generatePdfReceipt(Receipt receipt) {
        log.info("Generating PDF receipt: {}", receipt.getReceiptNumber());

        try {
            // Generate HTML content for receipt
            String htmlContent = generateHtmlReceipt(receipt);

            // Create S3 key
            String s3Key = createReceiptS3Key(receipt);

            // Convert HTML to bytes
            byte[] htmlBytes = htmlContent.getBytes();
            InputStream inputStream = new ByteArrayInputStream(htmlBytes);

            // Upload to S3
            String s3Url = s3Service.uploadFileFromStream(
                    inputStream,
                    s3Key,
                    "text/html",
                    htmlBytes.length
            );

            log.info("Receipt generated and stored in S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Error generating PDF receipt", e);
            throw new RuntimeException("Failed to generate receipt: " + e.getMessage());
        }
    }

    /**
     * Create S3 key (path) for receipt
     */
    private String createReceiptS3Key(Receipt receipt) {
        // Format: receipts/YYYY/MM/RECEIPT_NUMBER.html
        String yearMonth = receipt.getReceiptYearMonth();
        String year = yearMonth.split("-")[0];
        String month = yearMonth.split("-")[1];
        String filename = receipt.getReceiptNumber().replace("RECIPT-", "RECEIPT_").replace("/", "_") + ".html";

        return String.format("receipts/%s/%s/%s", year, month, filename);
    }

    /**
     * Generate HTML content for receipt
     */
    private String generateHtmlReceipt(Receipt receipt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Payment Receipt</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 3px solid #333; padding-bottom: 10px; }\n");
        html.append(".header h1 { margin: 0; color: #333; }\n");
        html.append(".receipt-number { font-size: 12px; color: #666; margin-top: 5px; }\n");
        html.append(".section { margin: 20px 0; }\n");
        html.append(".section h2 { font-size: 14px; color: #333; border-bottom: 1px solid #ddd; padding-bottom: 5px; margin-bottom: 10px; }\n");
        html.append(".row { display: flex; justify-content: space-between; margin: 8px 0; padding: 5px 0; border-bottom: 1px solid #eee; }\n");
        html.append(".row-label { font-weight: bold; width: 40%; }\n");
        html.append(".row-value { width: 60%; text-align: right; }\n");
        html.append(".amount { font-size: 16px; font-weight: bold; color: #27ae60; }\n");
        html.append(".status-paid { color: #27ae60; font-weight: bold; }\n");
        html.append(".status-pending { color: #e74c3c; font-weight: bold; }\n");
        html.append(".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; border-top: 1px solid #ddd; padding-top: 10px; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");

        // Header
        html.append("<div class='header'>\n");
        html.append("<h1>PAYMENT RECEIPT</h1>\n");
        html.append("<div class='receipt-number'>Receipt #: ").append(receipt.getReceiptNumber()).append("</div>\n");
        html.append("</div>\n");

        // Receipt Details
        html.append("<div class='section'>\n");
        html.append("<h2>Receipt Information</h2>\n");
        html.append("<div class='row'><div class='row-label'>Receipt Number:</div><div class='row-value'>").append(receipt.getReceiptNumber()).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Issued Date:</div><div class='row-value'>").append(receipt.getIssuedDate().format(formatter)).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Payment Date:</div><div class='row-value'>").append(receipt.getPaidDate() != null ? receipt.getPaidDate().format(formatter) : "N/A").append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Status:</div><div class='row-value ").append(receipt.getStatus().equals("CONFIRMED") ? "status-paid" : "status-pending").append("'>").append(receipt.getStatus()).append("</div></div>\n");
        html.append("</div>\n");

        // Customer Details
        html.append("<div class='section'>\n");
        html.append("<h2>Customer Information</h2>\n");
        html.append("<div class='row'><div class='row-label'>Customer Name:</div><div class='row-value'>").append(receipt.getCustomer().getName()).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Customer ID:</div><div class='row-value'>").append(receipt.getCustomer().getCustomerId()).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Phone:</div><div class='row-value'>").append(receipt.getCustomer().getCustomerId()).append("</div></div>\n");
        html.append("</div>\n");

        // Loan & Payment Details
        html.append("<div class='section'>\n");
        html.append("<h2>Loan & Payment Details</h2>\n");
        html.append("<div class='row'><div class='row-label'>Loan ID:</div><div class='row-value'>").append(receipt.getLoan().getId()).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Loan Amount:</div><div class='row-value'>₹ ").append(String.format("%.2f", receipt.getLoan().getLoanAmount())).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Total Amount:</div><div class='row-value'>₹ ").append(String.format("%.2f", receipt.getLoan().getTotalAmount())).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Payment Method:</div><div class='row-value'>").append(receipt.getPaymentMethod()).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Payment Mode:</div><div class='row-value'>").append(receipt.getPaymentMode()).append("</div></div>\n");
        html.append("</div>\n");

        // Amount Details
        html.append("<div class='section'>\n");
        html.append("<h2>Amount Details</h2>\n");
        html.append("<div class='row'><div class='row-label'>Amount Paid:</div><div class='row-value amount'>₹ ").append(String.format("%.2f", receipt.getAmountPaid())).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>Remaining Amount:</div><div class='row-value'>₹ ").append(String.format("%.2f", receipt.getLoan().getRemainingAmount())).append("</div></div>\n");
        html.append("<div class='row'><div class='row-label'>EMI Status:</div><div class='row-value'>").append(receipt.getLoan().getPaidEmis()).append(" of ").append(receipt.getLoan().getTotalEmis()).append(" paid</div></div>\n");

        if (receipt.getTransactionId() != null && !receipt.getTransactionId().isEmpty()) {
            html.append("<div class='row'><div class='row-label'>Transaction ID:</div><div class='row-value'>").append(receipt.getTransactionId()).append("</div></div>\n");
        }

        html.append("</div>\n");

        // Footer
        html.append("<div class='footer'>\n");
        html.append("<p>This is an electronically generated receipt. No signature is required.</p>\n");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(formatter)).append("</p>\n");
        html.append("<p>Receipt stored in AWS S3 Cloud Storage</p>\n");
        html.append("<p>Thank you for your payment!</p>\n");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * Generate text-based receipt (simpler version)
     */
    public String generateTextReceipt(Receipt receipt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        StringBuilder text = new StringBuilder();
        text.append("================== PAYMENT RECEIPT ==================\n\n");
        text.append("Receipt Number: ").append(receipt.getReceiptNumber()).append("\n");
        text.append("Status: ").append(receipt.getStatus()).append("\n");
        text.append("Issued Date: ").append(receipt.getIssuedDate().format(formatter)).append("\n\n");

        text.append("--- CUSTOMER INFORMATION ---\n");
        text.append("Name: ").append(receipt.getCustomer().getName()).append("\n");
        text.append("ID: ").append(receipt.getCustomer().getCustomerId()).append("\n");
        text.append("Phone: ").append(receipt.getCustomer().getCustomerId()).append("\n\n");

        text.append("--- LOAN DETAILS ---\n");
        text.append("Loan ID: ").append(receipt.getLoan().getId()).append("\n");
        text.append("Loan Amount: ₹ ").append(String.format("%.2f", receipt.getLoan().getLoanAmount())).append("\n");
        text.append("Total Amount: ₹ ").append(String.format("%.2f", receipt.getLoan().getTotalAmount())).append("\n\n");

        text.append("--- PAYMENT DETAILS ---\n");
        text.append("Amount Paid: ₹ ").append(String.format("%.2f", receipt.getAmountPaid())).append("\n");
        text.append("Payment Method: ").append(receipt.getPaymentMethod()).append("\n");
        text.append("Payment Mode: ").append(receipt.getPaymentMode()).append("\n");

        if (receipt.getTransactionId() != null && !receipt.getTransactionId().isEmpty()) {
            text.append("Transaction ID: ").append(receipt.getTransactionId()).append("\n");
        }

        text.append("\n--- EMI STATUS ---\n");
        text.append("Paid EMIs: ").append(receipt.getLoan().getPaidEmis()).append(" of ").append(receipt.getLoan().getTotalEmis()).append("\n");
        text.append("Remaining Amount: ₹ ").append(String.format("%.2f", receipt.getLoan().getRemainingAmount())).append("\n\n");

        text.append("====================================================\n");
        text.append("Stored in AWS S3 Cloud Storage\n");
        text.append("Thank you for your payment!\n");

        return text.toString();
    }

    /**
     * Download receipt from S3
     */
    public InputStream downloadReceiptFromS3(Receipt receipt) {
        String s3Key = createReceiptS3Key(receipt);
        return s3Service.downloadFile(s3Key);
    }

    /**
     * Delete receipt from S3
     */
    public void deleteReceiptFromS3(Receipt receipt) {
        String s3Key = createReceiptS3Key(receipt);
        s3Service.deleteFile(s3Key);
    }
}
