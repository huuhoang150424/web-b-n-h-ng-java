package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 50, message = "Tên sản phẩm không được dài quá 50 ký tự")
    private String productName;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải là số dương")
    private Float price;

    @NotBlank(message = "Ảnh thumb không được để trống")
    @Size(max = 300, message = "Ảnh thumb không được dài quá 300 ký tự")
    private String thumbImage;

    @NotNull(message = "Số lượng không được để trống")
    @PositiveOrZero(message = "Số lượng phải là số không âm")
    private Integer stock;

    private List<String> imageUrls;

    @NotBlank(message = "ID danh mục không được để trống")
    private String categoryId;

    private String description;

    private List<AttributeData> attributes;

    @Data
    public static class AttributeData {
        @NotBlank(message = "Tên thuộc tính không được để trống")
        private String attributeName;

        private String value;
    }
}