package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.DTO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base Controller - Provides common response handling utilities
 * All controllers should extend this class for consistent API responses
 */
public abstract class BaseController {

    /**
     * Creates a success response with data
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(String message, T data, HttpStatus status) {
        return new ResponseEntity<>(ApiResponse.success(message, data), status);
    }

    /**
     * Creates a success response without data (status 200)
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return success(message, null, HttpStatus.OK);
    }

    /**
     * Creates a created response (201)
     */
    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return success(message, data, HttpStatus.CREATED);
    }

    /**
     * Creates an error response (status 400)
     */
    protected <T> ResponseEntity<ApiResponse<T>> badRequest(String message, String error) {
        return new ResponseEntity<>(ApiResponse.error(message, error), HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates an error response without details
     */
    protected <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a not found response (404)
     */
    protected <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a conflict response (409)
     */
    protected <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.CONFLICT);
    }

    /**
     * Creates an internal server error response (500)
     */
    protected <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates that an object is not null
     * throws IllegalArgumentException if null
     */
    protected void validateNotNull(Object object, String fieldName) {
        if (object == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a string is not null or empty
     */
    protected void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
