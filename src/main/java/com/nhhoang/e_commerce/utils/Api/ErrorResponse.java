package com.nhhoang.e_commerce.utils.Api;

import java.util.Map;

public class ErrorResponse {
    private boolean success = false;
    private int status = 400;
    private String errorMessage;
    private Map<String, String> errors;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public ErrorResponse(String errorMessage, Map<String, String> errors) {
        this.errorMessage = errorMessage;
        this.errors = errors;
    }

    public ErrorResponse(String errorMessage, int status) {
        this.errorMessage = errorMessage;
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}