package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull(message = "Rating là bắt buộc")
    @Min(value = 1, message = "Rating phải từ 1 trở lên")
    @Max(value = 5, message = "Rating tối đa là 5")
    private Integer rating;
}