package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

@Data
public class CartRequestVnPay {
    private String id;
    private ProductRequestVnPay product;
    private Integer quantity;
}