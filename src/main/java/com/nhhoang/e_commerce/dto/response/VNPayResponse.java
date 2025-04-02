package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class VNPayResponse {
    private String paymentUrl;
    private String status;
    private String message;
}