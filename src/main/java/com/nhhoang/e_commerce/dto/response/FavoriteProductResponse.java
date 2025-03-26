package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class FavoriteProductResponse {
    private String id;
    private String userId;
    private ProductFavoriteResponse product;
}