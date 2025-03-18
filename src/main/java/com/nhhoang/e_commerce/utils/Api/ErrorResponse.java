package com.nhhoang.e_commerce.utils.Api;

public class ErrorResponse {
    private boolean success = false;
    private int status = 400;
    private String errorMessage;
    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}
