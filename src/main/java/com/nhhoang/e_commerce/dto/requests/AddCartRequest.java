package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

@Data
public class AddCartRequest {
    private String productId;
    private Integer quantity;
}