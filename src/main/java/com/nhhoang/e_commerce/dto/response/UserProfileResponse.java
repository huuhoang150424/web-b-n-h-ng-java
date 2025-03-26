package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhhoang.e_commerce.entity.User.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileResponse {
    private String name;
    private String email;
    private Gender gender;
    private String avatar;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}