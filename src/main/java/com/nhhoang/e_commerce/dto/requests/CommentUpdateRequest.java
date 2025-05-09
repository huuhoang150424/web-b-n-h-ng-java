package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequest {
    @NotBlank(message = "Nội dung bình luận là bắt buộc")
    private String comment;
}