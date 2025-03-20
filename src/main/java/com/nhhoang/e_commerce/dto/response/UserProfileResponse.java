package com.nhhoang.e_commerce.dto.response;

import com.nhhoang.e_commerce.entity.User.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileResponse {
    private String name;
    private String email;
    private Gender gender;
    private String avatar;
    private LocalDate birthDate;
}