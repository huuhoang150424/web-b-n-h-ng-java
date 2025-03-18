package com.nhhoang.e_commerce.utils.Api;

import java.util.Map;

public class SuccessResponse {
    private boolean success = true;
    private int status = 200;
    private String message;
    private Map<String, Object> result;

    public SuccessResponse(String message, Map<String, Object> result) {
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Object> getResult() { return result; }
}
