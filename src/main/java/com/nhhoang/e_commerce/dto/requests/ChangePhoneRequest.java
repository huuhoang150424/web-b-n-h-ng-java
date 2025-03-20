package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePhoneRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
}