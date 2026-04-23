package com.example.LoanManagementApp.DTO;

public class LoanRiskAssessmentResult {

    private boolean modelChecked;
    private boolean eligibleForAutoApproval;
    private Integer predictedClass;
    private Double approvalProbability;
    private String recommendedDecision;
    private String message;

    public boolean isModelChecked() {
        return modelChecked;
    }

    public void setModelChecked(boolean modelChecked) {
        this.modelChecked = modelChecked;
    }

    public boolean isEligibleForAutoApproval() {
        return eligibleForAutoApproval;
    }

    public void setEligibleForAutoApproval(boolean eligibleForAutoApproval) {
        this.eligibleForAutoApproval = eligibleForAutoApproval;
    }

    public Integer getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(Integer predictedClass) {
        this.predictedClass = predictedClass;
    }

    public Double getApprovalProbability() {
        return approvalProbability;
    }

    public void setApprovalProbability(Double approvalProbability) {
        this.approvalProbability = approvalProbability;
    }

    public String getRecommendedDecision() {
        return recommendedDecision;
    }

    public void setRecommendedDecision(String recommendedDecision) {
        this.recommendedDecision = recommendedDecision;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
