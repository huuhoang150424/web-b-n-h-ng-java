package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhhoang.e_commerce.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailResponse {
    private String id;
    private String slug;
    private String productName;
    private Float price;
    private String thumbImage;
    private Integer stock;
    private List<String> imageUrls;
    private String description;
    private Product.Status status;
    private CategoryResponse category;
    private List<ProductAttributeResponse> productAttributes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

    @Data
    public static class CategoryResponse {
        private String id;
        private String categoryName;
        private String image;
    }

    @Data
    public static class ProductAttributeResponse {
        private String id;
        private String attributeName;
        private String value;
    }
}