package com.nhhoang.e_commerce.dto.requests;

import com.nhhoang.e_commerce.entity.Coupon.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    private Float discountValue;

    private Float minOrderAmount;
    private Float maxDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
