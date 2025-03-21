
package com.nhhoang.e_commerce.dto.response;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.entity.User.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GetUserResponse {
    private String id;
    private String name;
    private String email;
    private Gender gender;
    private String avatar;
    private Role role;
    private List<String> address;
    private String phone;
    private LocalDate birthDate;
}