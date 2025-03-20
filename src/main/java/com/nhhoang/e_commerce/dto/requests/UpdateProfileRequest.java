package com.nhhoang.e_commerce.dto.requests;

import com.nhhoang.e_commerce.entity.User.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    @Size(max = 20, message = "Tên không được dài quá 20 ký tự")
    private String name;

    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Email không được dài quá 50 ký tự")
    private String email;

    private Gender gender;

    @Size(max = 150, message = "Avatar không được dài quá 150 ký tự")
    private String avatar;

    private LocalDate birthDate;
}