package com.nhhoang.e_commerce.dto.response;

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
    private LocalDate birthDate;
    private String gender;
    private String phone;
}