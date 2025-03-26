package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhhoang.e_commerce.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductSearchResponse {
    private String id;
    private String slug;
    private String productName;
    private Float price;
    private String thumbImage;
    private Integer stock;
    private List<String> imageUrls;
    private String description;
    private Product.Status status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}