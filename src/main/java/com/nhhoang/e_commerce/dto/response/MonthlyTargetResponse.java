package com.nhhoang.e_commerce.dto.response;

import lombok.Data;

@Data
public class MonthlyTargetResponse {
    private Integer userCount;
    private Integer orderCount;
    private Integer commentCount;
    private Float totalRevenue;
}