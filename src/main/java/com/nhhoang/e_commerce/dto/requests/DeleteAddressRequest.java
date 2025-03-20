package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAddressRequest {
    @NotBlank(message = "Địa chỉ cần xóa không được để trống")
    private String address;
}