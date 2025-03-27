package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderCodRequest {
    private List<CartItemRequest> carts;
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;

    @Data
    public static class CartItemRequest {
        private String id;
        private ProductRequest product;
        private Integer quantity;
    }

    @Data
    public static class ProductRequest {
        private String id;
        private Float price;
    }
}