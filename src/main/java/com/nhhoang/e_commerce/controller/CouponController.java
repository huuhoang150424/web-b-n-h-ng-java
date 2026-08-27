package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.ApplyCouponRequest;
import com.nhhoang.e_commerce.dto.requests.CreateCouponRequest;
import com.nhhoang.e_commerce.dto.response.CouponResponse;
import com.nhhoang.e_commerce.service.CouponService;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/create")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(new SuccessResponse("Tạo mã giảm giá thành công", response));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCoupons() {
        List<CouponResponse> response = couponService.getAllCoupons();
        return ResponseEntity.ok(new SuccessResponse("Lấy danh sách mã giảm giá thành công", response));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        CouponResponse response = couponService.validateAndCalculateCoupon(request);
        return ResponseEntity.ok(new SuccessResponse("Áp dụng mã giảm giá thành công", response));
    }
}
