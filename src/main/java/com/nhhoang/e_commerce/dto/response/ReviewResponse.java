package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class ReviewResponse {
    private ProductReviewResponse product;
    private UserReviewResponse user;
    private Integer rating;
    private String comment;

    @Data
    public static class UserReviewResponse {
        private String avatar;
        private String name;
    }

    @Data
    public static class ProductReviewResponse {
        private String productName;
        private String thumbImage;
    }
}