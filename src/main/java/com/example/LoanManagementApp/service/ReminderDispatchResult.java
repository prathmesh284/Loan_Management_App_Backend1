package com.example.LoanManagementApp.service;

public class ReminderDispatchResult {

    private final boolean success;
    private final String channel;
    private final String providerMessageId;
    private final String errorMessage;

    private ReminderDispatchResult(boolean success, String channel, String providerMessageId, String errorMessage) {
        this.success = success;
        this.channel = channel;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
    }

    public static ReminderDispatchResult success(String channel, String providerMessageId) {
        return new ReminderDispatchResult(true, channel, providerMessageId, null);
    }

    public static ReminderDispatchResult failure(String channel, String errorMessage) {
        return new ReminderDispatchResult(false, channel, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getChannel() {
        return channel;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
