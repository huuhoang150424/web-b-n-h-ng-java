package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class RegisterRequests {
    @Schema(example = "newuser@gmail.com")
    private String email;

    @Schema(example = "12345678")
    private String password;

    @Schema(example = "12345678")
    private String confirmPassword;

    @Schema(example = "Nguyễn Văn Mới")
    private String name;
}