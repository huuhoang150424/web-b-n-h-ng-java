package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class ProductCartResponse {
    private String id;
    private String productName;
    private Float price;
    private String thumbImage;
}