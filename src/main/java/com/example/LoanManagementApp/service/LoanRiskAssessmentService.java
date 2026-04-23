package com.example.LoanManagementApp.service;

import com.example.LoanManagementApp.DTO.LoanRiskAssessmentResult;
import com.example.LoanManagementApp.model.Customer;
import com.example.LoanManagementApp.model.Document;
import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.model.Loan;
import com.example.LoanManagementApp.model.Receipt;
import com.example.LoanManagementApp.repo.DocumentRepo;
import com.example.LoanManagementApp.repo.EmiRepo;
import com.example.LoanManagementApp.repo.LoanRepo;
import com.example.LoanManagementApp.repo.ReceiptRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class LoanRiskAssessmentService {

    @Value("${loan.risk.model.enabled:true}")
    private boolean modelEnabled;

    @Value("${loan.risk.model.url:}")
    private String modelUrl;

    @Value("${loan.risk.model.token:}")
    private String modelToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LoanRepo loanRepo;
    private final DocumentRepo documentRepo;
    private final EmiRepo emiRepo;
    private final ReceiptRepo receiptRepo;

    public LoanRiskAssessmentService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            LoanRepo loanRepo,
            DocumentRepo documentRepo,
            EmiRepo emiRepo,
            ReceiptRepo receiptRepo
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.loanRepo = loanRepo;
        this.documentRepo = documentRepo;
        this.emiRepo = emiRepo;
        this.receiptRepo = receiptRepo;
    }

    public LoanRiskAssessmentResult assessLoanApplication(Customer customer, Loan proposedLoan, Map<String, Object> request) {
        if (!modelEnabled || modelUrl == null || modelUrl.isBlank()) {
            throw new IllegalStateException("Loan risk model is not configured");
        }

        Map<String, Object> features = buildFeatures(customer, proposedLoan, request);
        List<Object> payload = buildPayload(features);
        Map<String, Object> prediction = invokePrediction(payload);

        LoanRiskAssessmentResult result = new LoanRiskAssessmentResult();
        result.setModelChecked(true);
        result.setPredictedClass(toInteger(prediction.get("predicted_class")));
        result.setApprovalProbability(toDouble(prediction.get("approval_probability")));

        String recommendedDecision = Optional.ofNullable(prediction.get("recommended_decision"))
                .map(String::valueOf)
                .orElse("MANUAL REVIEW");
        result.setRecommendedDecision(recommendedDecision);
        result.setEligibleForAutoApproval("APPROVE".equalsIgnoreCase(recommendedDecision));

        if (result.isEligibleForAutoApproval()) {
            result.setMessage("Loan applicant passed the ML credibility check.");
        } else if ("MANUAL REVIEW".equalsIgnoreCase(recommendedDecision)) {
            result.setMessage("Loan applicant requires manual review before loan creation.");
        } else {
            result.setMessage("Loan applicant was flagged as high risk by the ML model.");
        }

        return result;
    }

    private Map<String, Object> buildFeatures(Customer customer, Loan proposedLoan, Map<String, Object> request) {
        List<Loan> existingLoans = loanRepo.findByCustomerId(customer.getCustomerId());
        List<Document> documents = documentRepo.findByCustomerPhone(customer.getCustomerId());
        List<Receipt> receipts = receiptRepo.findByCustomerCustomerId(customer.getCustomerId());
        List<Emi> paidEmis = emiRepo.findPaidEmisForCustomer(customer.getCustomerId());
        List<Loan> branchLoans = loanRepo.findByBranchId(customer.getBranch().getId());

        int verifiedDocs = (int) documents.stream().filter(doc -> Boolean.TRUE.equals(doc.getIsVerified())).count();
        boolean hasIdentityDoc = documents.stream().anyMatch(doc -> isAnyType(doc, "IDENTITY", "KYC"));
        boolean hasAddressDoc = documents.stream().anyMatch(doc -> isAnyType(doc, "ADDRESS", "KYC"));
        boolean hasIncomeDoc = documents.stream().anyMatch(doc -> isAnyType(doc, "INCOME"));

        long closedLoansCount = existingLoans.stream().filter(loan -> "CLOSED".equalsIgnoreCase(loan.getStatus())).count();
        long defaultedLoansCount = existingLoans.stream().filter(loan -> "DEFAULTED".equalsIgnoreCase(loan.getStatus())).count();
        long activeLoansCount = existingLoans.stream().filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus())).count();

        double avgPastLoanAmount = existingLoans.stream()
                .map(Loan::getLoanAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxPastLoanAmount = existingLoans.stream()
                .map(Loan::getLoanAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double totalOutstandingAmount = existingLoans.stream()
                .filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus()))
                .map(Loan::getRemainingAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        LocalDate lastLoanDate = existingLoans.stream()
                .map(Loan::getLoanDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        long daysSinceLastLoan = lastLoanDate == null ? 999 : ChronoUnit.DAYS.between(lastLoanDate, LocalDate.now());
        long loanFrequencyLast12Months = existingLoans.stream()
                .map(Loan::getLoanDate)
                .filter(Objects::nonNull)
                .filter(date -> !date.isBefore(LocalDate.now().minusMonths(12)))
                .count();

        int totalEmiExpected = existingLoans.stream()
                .map(Loan::getTotalEmis)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalEmiPaid = existingLoans.stream()
                .map(Loan::getPaidEmis)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        List<Emi> allEmis = new ArrayList<>();
        for (Loan loan : existingLoans) {
            allEmis.addAll(emiRepo.findByLoanId(loan.getId()));
        }

        long overdueEmiCount = allEmis.stream().filter(emi -> "OVERDUE".equalsIgnoreCase(emi.getStatus())).count();
        long defaultEmiCount = allEmis.stream().filter(emi -> "DEFAULT".equalsIgnoreCase(emi.getStatus())).count();
        long pendingEmiCount = allEmis.stream().filter(emi -> "PENDING".equalsIgnoreCase(emi.getStatus())).count();
        long latePaymentCount = overdueEmiCount + defaultEmiCount;

        double emiPaymentRatio = totalEmiExpected == 0 ? 0.0 : round4((double) totalEmiPaid / totalEmiExpected);
        double latePaymentRatio = totalEmiExpected == 0 ? 0.0 : round4((double) latePaymentCount / totalEmiExpected);

        double totalAmountPaidSoFar = paidEmis.stream()
                .map(Emi::getAmountPaid)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double avgEmiPaidAmount = paidEmis.stream()
                .map(Emi::getAmountPaid)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgDaysBetweenEmiPayments = calculateAverageDaysBetweenPayments(paidEmis);

        long successfulReceiptCount = receipts.stream().filter(receipt -> "CONFIRMED".equalsIgnoreCase(receipt.getStatus())).count();
        long failedReceiptCount = receipts.stream().filter(receipt -> "FAILED".equalsIgnoreCase(receipt.getStatus())).count();
        long cancelledReceiptCount = receipts.stream().filter(receipt -> "CANCELLED".equalsIgnoreCase(receipt.getStatus())).count();

        long manualPayments = receipts.stream()
                .filter(receipt -> "MANUAL".equalsIgnoreCase(receipt.getPaymentMode())
                        || "CASH".equalsIgnoreCase(receipt.getPaymentMethod())
                        || "CHEQUE".equalsIgnoreCase(receipt.getPaymentMethod()))
                .count();

        long onlinePayments = receipts.stream()
                .filter(receipt -> !"MANUAL".equalsIgnoreCase(receipt.getPaymentMode())
                        || "UPI".equalsIgnoreCase(receipt.getPaymentMethod())
                        || "ONLINE_TRANSFER".equalsIgnoreCase(receipt.getPaymentMethod()))
                .count();

        int paymentEvents = receipts.size();
        double manualPaymentRatio = paymentEvents == 0 ? 0.0 : round4((double) manualPayments / paymentEvents);
        double onlinePaymentRatio = paymentEvents == 0 ? 0.0 : round4((double) onlinePayments / paymentEvents);
        double paymentFailureRatio = paymentEvents == 0 ? 0.0 : round4((double) failedReceiptCount / paymentEvents);

        int duplicatePaymentAttemptFlag = hasDuplicateTransaction(receipts) ? 1 : 0;

        long branchDefaultCount = branchLoans.stream().filter(loan -> "DEFAULTED".equalsIgnoreCase(loan.getStatus())).count();
        double branchDefaultRate = branchLoans.isEmpty() ? 0.0 : round4((double) branchDefaultCount / branchLoans.size());
        double branchAvgLoanAmount = branchLoans.stream()
                .map(Loan::getLoanAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        int identitySignals = 0;
        identitySignals += hasText(customer.getEmail()) ? 1 : 0;
        identitySignals += hasText(customer.getAadharNumber()) ? 1 : 0;
        identitySignals += hasText(customer.getPanNumber()) ? 1 : 0;
        identitySignals += hasIdentityDoc ? 1 : 0;
        identitySignals += hasAddressDoc ? 1 : 0;
        identitySignals += hasIncomeDoc ? 1 : 0;
        double kycCompletenessScore = round4((identitySignals / 6.0) * 100.0);
        double kycVerificationRatio = documents.isEmpty() ? 0.0 : round4((double) verifiedDocs / documents.size());

        Map<String, Object> features = new HashMap<>();
        features.put("branch_id", customer.getBranch().getBranchCode());
        features.put("city", customer.getBranch().getCity());
        features.put("customer_age", getNumber(request, "customerAge", 35));
        features.put("address_length", customer.getAddress() == null ? 0 : customer.getAddress().trim().length());
        features.put("has_valid_email", hasValidEmail(customer.getEmail()) ? 1 : 0);
        features.put("has_aadhar", hasText(customer.getAadharNumber()) ? 1 : 0);
        features.put("has_pan", hasText(customer.getPanNumber()) ? 1 : 0);
        features.put("total_documents_uploaded", documents.size());
        features.put("verified_documents_count", verifiedDocs);
        features.put("has_identity_doc", hasIdentityDoc ? 1 : 0);
        features.put("has_address_doc", hasAddressDoc ? 1 : 0);
        features.put("has_income_doc", hasIncomeDoc ? 1 : 0);
        features.put("kyc_completeness_score", kycCompletenessScore);
        features.put("kyc_verification_ratio", kycVerificationRatio);
        features.put("past_loans_count", existingLoans.size());
        features.put("closed_loans_count", closedLoansCount);
        features.put("defaulted_loans_count", defaultedLoansCount);
        features.put("active_loans_count", activeLoansCount);
        features.put("avg_past_loan_amount", avgPastLoanAmount);
        features.put("max_past_loan_amount", maxPastLoanAmount);
        features.put("total_outstanding_amount", totalOutstandingAmount);
        features.put("days_since_last_loan", daysSinceLastLoan);
        features.put("loan_frequency_last_12_months", loanFrequencyLast12Months);
        features.put("total_emi_expected_count", totalEmiExpected);
        features.put("total_emi_paid_count", totalEmiPaid);
        features.put("emi_payment_ratio", emiPaymentRatio);
        features.put("overdue_emi_count", overdueEmiCount);
        features.put("default_emi_count", defaultEmiCount);
        features.put("pending_emi_count", pendingEmiCount);
        features.put("avg_days_between_emi_payments", avgDaysBetweenEmiPayments);
        features.put("late_payment_count", latePaymentCount);
        features.put("late_payment_ratio", latePaymentRatio);
        features.put("total_amount_paid_so_far", totalAmountPaidSoFar);
        features.put("avg_emi_paid_amount", avgEmiPaidAmount);
        features.put("successful_receipt_count", successfulReceiptCount);
        features.put("failed_receipt_count", failedReceiptCount);
        features.put("cancelled_receipt_count", cancelledReceiptCount);
        features.put("manual_payment_ratio", manualPaymentRatio);
        features.put("online_payment_ratio", onlinePaymentRatio);
        features.put("payment_failure_ratio", paymentFailureRatio);
        features.put("duplicate_payment_attempt_flag", duplicatePaymentAttemptFlag);
        features.put("branch_default_rate", branchDefaultRate);
        features.put("branch_avg_loan_amount", branchAvgLoanAmount);
        features.put("requested_loan_amount", safeDouble(proposedLoan.getLoanAmount()));
        features.put("requested_tenure", proposedLoan.getTenure() == null ? 0 : proposedLoan.getTenure());
        features.put("requested_gold_weight", safeDouble(proposedLoan.getWeight()));
        features.put("requested_gold_price", safeDouble(proposedLoan.getGoldPrice()));
        features.put("requested_gold_purity", proposedLoan.getGoldPurity());
        features.put("requested_gold_item_type", proposedLoan.getGoldItemType());
        features.put("requested_ltv", safeDouble(proposedLoan.getLtv()));
        features.put("requested_interest_rate", safeDouble(proposedLoan.getInterestRate()));
        return features;
    }

    private List<Object> buildPayload(Map<String, Object> features) {
        List<Object> payload = new ArrayList<>();
        payload.add(features.get("branch_id"));
        payload.add(features.get("city"));
        payload.add(features.get("customer_age"));
        payload.add(features.get("address_length"));
        payload.add(intAsBoolean(features.get("has_valid_email")));
        payload.add(intAsBoolean(features.get("has_aadhar")));
        payload.add(intAsBoolean(features.get("has_pan")));
        payload.add(features.get("total_documents_uploaded"));
        payload.add(features.get("verified_documents_count"));
        payload.add(intAsBoolean(features.get("has_identity_doc")));
        payload.add(intAsBoolean(features.get("has_address_doc")));
        payload.add(intAsBoolean(features.get("has_income_doc")));
        payload.add(features.get("kyc_completeness_score"));
        payload.add(features.get("kyc_verification_ratio"));
        payload.add(features.get("past_loans_count"));
        payload.add(features.get("closed_loans_count"));
        payload.add(features.get("defaulted_loans_count"));
        payload.add(features.get("active_loans_count"));
        payload.add(features.get("avg_past_loan_amount"));
        payload.add(features.get("max_past_loan_amount"));
        payload.add(features.get("total_outstanding_amount"));
        payload.add(features.get("days_since_last_loan"));
        payload.add(features.get("loan_frequency_last_12_months"));
        payload.add(features.get("total_emi_expected_count"));
        payload.add(features.get("total_emi_paid_count"));
        payload.add(features.get("emi_payment_ratio"));
        payload.add(features.get("overdue_emi_count"));
        payload.add(features.get("default_emi_count"));
        payload.add(features.get("pending_emi_count"));
        payload.add(features.get("avg_days_between_emi_payments"));
        payload.add(features.get("late_payment_count"));
        payload.add(features.get("late_payment_ratio"));
        payload.add(features.get("total_amount_paid_so_far"));
        payload.add(features.get("avg_emi_paid_amount"));
        payload.add(features.get("successful_receipt_count"));
        payload.add(features.get("failed_receipt_count"));
        payload.add(features.get("cancelled_receipt_count"));
        payload.add(features.get("manual_payment_ratio"));
        payload.add(features.get("online_payment_ratio"));
        payload.add(features.get("payment_failure_ratio"));
        payload.add(intAsBoolean(features.get("duplicate_payment_attempt_flag")));
        payload.add(features.get("branch_default_rate"));
        payload.add(features.get("branch_avg_loan_amount"));
        payload.add(features.get("requested_loan_amount"));
        payload.add(features.get("requested_tenure"));
        payload.add(features.get("requested_gold_weight"));
        payload.add(features.get("requested_gold_price"));
        payload.add(features.get("requested_gold_purity"));
        payload.add(features.get("requested_gold_item_type"));
        payload.add(features.get("requested_ltv"));
        payload.add(features.get("requested_interest_rate"));
        return payload;
    }

    private Map<String, Object> invokePrediction(List<Object> payload) {
        String url = modelUrl.trim();
        if (url.contains("/call/")) {
            return invokeCallPredict(url, payload);
        }
        return invokeRunPredict(url, payload);
    }

    private Map<String, Object> invokeRunPredict(String url, List<Object> payload) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("data", payload), buildHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return extractPredictionBody(response.getBody());
    }

    private Map<String, Object> invokeCallPredict(String url, List<Object> payload) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("data", payload), buildHeaders());
        ResponseEntity<Map> submitResponse = restTemplate.postForEntity(url, request, Map.class);
        if (submitResponse.getBody() == null || submitResponse.getBody().get("event_id") == null) {
            throw new IllegalStateException("Loan risk model did not return an event id");
        }

        String resultUrl = url.endsWith("/")
                ? url + submitResponse.getBody().get("event_id")
                : url + "/" + submitResponse.getBody().get("event_id");

        HttpEntity<Void> getRequest = new HttpEntity<>(buildHeaders());
        ResponseEntity<String> resultResponse = restTemplate.exchange(resultUrl, org.springframework.http.HttpMethod.GET, getRequest, String.class);
        return extractPredictionFromEventStream(resultResponse.getBody());
    }

    private Map<String, Object> extractPredictionFromEventStream(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Loan risk model returned an empty event stream");
        }

        String[] lines = responseBody.split("\\r?\\n");
        String lastDataLine = null;
        for (String line : lines) {
            if (line.startsWith("data: ")) {
                lastDataLine = line.substring(6).trim();
            }
        }

        if (lastDataLine == null || lastDataLine.isBlank()) {
            throw new IllegalStateException("Loan risk model event stream did not contain prediction data");
        }

        try {
            List<Object> values = objectMapper.readValue(lastDataLine, new TypeReference<>() {});
            if (values.isEmpty()) {
                throw new IllegalStateException("Loan risk model event stream returned an empty data array");
            }
            Object firstValue = values.get(0);
            if (firstValue instanceof Map<?, ?> prediction) {
                return castMap(prediction);
            }
            if (firstValue instanceof String predictionString) {
                return objectMapper.readValue(predictionString, new TypeReference<>() {});
            }
            throw new IllegalStateException("Unexpected loan risk model response format");
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse loan risk model event stream", e);
        }
    }

    private Map<String, Object> extractPredictionBody(Map body) {
        if (body == null) {
            throw new IllegalStateException("Loan risk model returned an empty response");
        }

        if (body.containsKey("predicted_class")) {
            return castMap(body);
        }

        Object data = body.get("data");
        if (data instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> prediction) {
                return castMap(prediction);
            }
            if (first instanceof String predictionString) {
                try {
                    return objectMapper.readValue(predictionString, new TypeReference<>() {});
                } catch (Exception e) {
                    throw new IllegalStateException("Loan risk model returned unparseable JSON output", e);
                }
            }
        }

        throw new IllegalStateException("Loan risk model response format is not supported");
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (modelToken != null && !modelToken.isBlank()) {
            headers.setBearerAuth(modelToken.trim());
        }
        return headers;
    }

    private boolean isAnyType(Document document, String... docTypes) {
        if (document == null || document.getDocType() == null) {
            return false;
        }
        for (String docType : docTypes) {
            if (docType.equalsIgnoreCase(document.getDocType())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateTransaction(List<Receipt> receipts) {
        Set<String> seen = new HashSet<>();
        for (Receipt receipt : receipts) {
            if (receipt.getTransactionId() == null || receipt.getTransactionId().isBlank()) {
                continue;
            }
            if (!seen.add(receipt.getTransactionId())) {
                return true;
            }
        }
        return false;
    }

    private double calculateAverageDaysBetweenPayments(List<Emi> paidEmis) {
        if (paidEmis.size() < 2) {
            return 30.0;
        }

        List<LocalDate> dates = paidEmis.stream()
                .map(Emi::getPaymentDate)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .toList();

        if (dates.size() < 2) {
            return 30.0;
        }

        long totalDays = 0;
        for (int i = 1; i < dates.size(); i++) {
            totalDays += ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
        }
        return round4((double) totalDays / (dates.size() - 1));
    }

    private boolean intAsBoolean(Object value) {
        return toInteger(value) == 1;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasValidEmail(String email) {
        return hasText(email) && email.contains("@") && email.contains(".");
    }

    private Number getNumber(Map<String, Object> source, String key, Number defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return 0;
    }

    private Double toDouble(Object value) {
        if (value instanceof Double doubleValue) {
            return doubleValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text);
        }
        return 0.0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
