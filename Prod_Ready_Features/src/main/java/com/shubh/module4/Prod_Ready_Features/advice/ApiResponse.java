package com.shubh.module4.Prod_Ready_Features.advice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// API Response Transformation
// Dual-purpose: Used for serializing outgoing controller data and deserializing incoming client data
@Getter
/* * 🟢 DESERIALIZATION SAFETY NET:
 * Gives Jackson full access to overwrite fields when receiving payloads from external services.
 * This ensures that the remote server's original timestamp overrides our local temporary timestamp.
 */
@Setter
public class ApiResponse<T> {

    @JsonFormat(pattern = "HH-mm-ss dd-MM-yyyy")
    private LocalDateTime timestamp;
    private T data;
    private ApiError error;

    /**
     * 1. OUTGOING RESPONSES: Called via the convenience constructors (this();)
     * to automatically stamp the local server's current time.
     * 2. INCOMING RESPONSES: Used by Jackson to create the initial empty shell.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Convenience Constructor for successful outgoing controller responses.
     */
    public ApiResponse(T data) {
        this();
        this.data = data;
    }

    /**
     * Convenience Constructor for outgoing error controller responses.
     */
    public ApiResponse(ApiError error) {
        this();
        this.error = error;
    }
}