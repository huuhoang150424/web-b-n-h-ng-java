package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductClientResponse {
    private String id;
    private String slug;
    private String productName;
    private Float price;
    private String thumbImage;
    private Integer stock;
    private List<String> imageUrls;
    private String description;
    private String status;
    private CategoryResponse category;
    private List<ProductAttributeResponse> productAttributes;
    private List<RatingResponse> ratings;
    private Double averageRating;
    private Boolean isFavorite;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}