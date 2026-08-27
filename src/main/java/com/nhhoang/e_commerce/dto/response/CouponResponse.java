package com.nhhoang.e_commerce.dto.response;

import com.nhhoang.e_commerce.entity.Coupon.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {
    private String id;
    private String code;
    private DiscountType discountType;
    private Float discountValue;
    private Float minOrderAmount;
    private Float maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private Float calculatedDiscount; // Optional calculated discount when applying
    private Float finalTotal;
}
