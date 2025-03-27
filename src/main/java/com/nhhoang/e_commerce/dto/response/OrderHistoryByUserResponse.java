package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryByUserResponse {
    private String id;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private OrderResponse order;

    @Data
    public static class OrderResponse {
        private String id;
        private Float totalAmount;
        private String status;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        private String shippingAddress;
        private String receiverName;
        private String receiverPhone;
        private String orderCode;
        private List<OrderDetailResponse> orderDetails;
    }

    @Data
    public static class OrderDetailResponse {
        private String id;
        private ProductResponse product;
        private Integer quantity;
        private Float price;
    }

    @Data
    public static class ProductResponse {
        private String productName;
        private Float price;
        private String thumbImage;
        private Integer stock;
        private CategoryResponse category;
    }

    @Data
    public static class CategoryResponse {
        private String id;
        private String categoryName;
        private String image;
    }
}