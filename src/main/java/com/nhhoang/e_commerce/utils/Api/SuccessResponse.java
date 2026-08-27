package com.nhhoang.e_commerce.utils.Api;

public class SuccessResponse {
    private boolean success = true;
    private int status = 200;
    private String message;
    private Object result;

    public SuccessResponse(String message, Object result) {
        this.message = message;
        this.result = result;
    }

    public SuccessResponse(String message, Object result, int status) {
        this.message = message;
        this.result = result;
        this.status = status;
    }

    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getResult() { return result; }
}
