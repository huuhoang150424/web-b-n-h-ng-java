package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Integer quantity;
    private String productId;
}