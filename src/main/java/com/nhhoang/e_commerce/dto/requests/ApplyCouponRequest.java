package com.nhhoang.e_commerce.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class ApplyCouponRequest {
    @Schema(example = "WELCOME100", description = "Mã giảm giá (WELCOME100, SUMMER20, VIPFLASH)")
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @Schema(example = "1500000", description = "Tổng số tiền đơn hàng")
    @NotNull(message = "Tổng giá trị đơn hàng không được để trống")
    private Float orderTotal;
}
