package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class CartItemResponse {
    private String id;
    private ProductCartResponse product;
    private Integer quantity;
}
