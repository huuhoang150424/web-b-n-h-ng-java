package com.nhhoang.e_commerce.dto.requests;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.entity.User.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 20, message = "Tên không được dài quá 20 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Email không được dài quá 50 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 100, message = "Mật khẩu không được dài quá 100 ký tự")
    private String password;

    private Gender gender;

    @Size(max = 150, message = "Avatar không được dài quá 150 ký tự")
    private String avatar;

    private Role role; // Thay isAdmin bằng role, có thể null (mặc định USER)
}