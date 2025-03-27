package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryResponse {
    private String id;
    private List<OrderHistoryByOrderResponse> orderHistories;

    @Data
    public static class OrderHistoryByOrderResponse {
        private String id;
        private String status;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime changedAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;

        private String orderId;
        private UserResponse changeBy;

        @Data
        public static class UserResponse {
            private String id;
            private String name;
            private String email;
        }
    }
}