package com.nhhoang.e_commerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListOrderChangeResponse {
    private String id;
    private UserResponse changeBy;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;

    @Data
    public static class UserResponse {
        private String name;
        private String avatar;
    }
}