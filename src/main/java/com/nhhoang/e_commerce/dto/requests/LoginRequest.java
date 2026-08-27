package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class LoginRequest {
    @Schema(example = "admin@gmail.com", description = "Email đăng nhập (admin@gmail.com hoặc user@gmail.com)")
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Schema(example = "12345678", description = "Mật khẩu đăng nhập")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}