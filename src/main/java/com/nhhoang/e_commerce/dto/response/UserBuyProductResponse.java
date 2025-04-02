package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class UserBuyProductResponse {
    private String name;
    private String email;
    private String avatar;
    private Integer orderCount;
}