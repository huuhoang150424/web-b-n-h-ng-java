package com.nhhoang.e_commerce.dto.requests;

import lombok.Data;

@Data
public class RegisterRequests {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;
}