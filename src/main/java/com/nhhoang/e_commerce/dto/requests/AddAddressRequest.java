package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddAddressRequest {
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
}