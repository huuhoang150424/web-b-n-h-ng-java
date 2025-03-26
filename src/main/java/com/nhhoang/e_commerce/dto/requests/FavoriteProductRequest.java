package com.nhhoang.e_commerce.dto.requests;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FavoriteProductRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;
}