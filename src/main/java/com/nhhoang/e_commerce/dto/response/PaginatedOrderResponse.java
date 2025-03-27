package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaginatedOrderResponse {
    private int totalItems;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private List<GetOrderResponse> data;

    public PaginatedOrderResponse(int totalItems, int currentPage, int totalPages, int pageSize, List<GetOrderResponse> data) {
        this.totalItems = totalItems;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.data = data;
    }

    @Data
    public static class GetOrderResponse {
        private String id;
        private UserOrderResponse user;
        private Float totalAmount;
        private String status;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        private String shippingAddress;
        private String receiverName;
        private String receiverPhone;
        private String orderCode;
    }

    @Data
    public static class UserOrderResponse {
        private String name;
        private String email;
    }
}