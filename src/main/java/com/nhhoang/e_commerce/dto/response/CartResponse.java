package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String id;
    private String userId;
    private List<CartItemResponse> cartItems;
}


