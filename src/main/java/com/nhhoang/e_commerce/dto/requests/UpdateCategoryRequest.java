package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String categoryName;
    private String image;
}