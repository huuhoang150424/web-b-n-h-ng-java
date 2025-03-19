package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String avatar;
    private List<String> address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String gender;
    private String phone;
}