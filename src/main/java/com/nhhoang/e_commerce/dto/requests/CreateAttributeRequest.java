package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAttributeRequest {
    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(max = 50, message = "Tên thuộc tính không được dài quá 50 ký tự")
    private String attributeName;
}