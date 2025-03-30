package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class CategoryClientResponse {
    private String id;
    private String categoryName;
    private Integer productCount;
}