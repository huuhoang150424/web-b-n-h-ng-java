package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class ProductAttributeResponse {
    private String id;
    private String attributeName;
    private String attributeValue;
}