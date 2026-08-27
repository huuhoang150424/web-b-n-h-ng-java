package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.ApplyCouponRequest;
import com.nhhoang.e_commerce.dto.requests.CreateCouponRequest;
import com.nhhoang.e_commerce.dto.response.CouponResponse;
import com.nhhoang.e_commerce.entity.Coupon;
import com.nhhoang.e_commerce.entity.Coupon.DiscountType;
import com.nhhoang.e_commerce.exception.AppCustomException;
import com.nhhoang.e_commerce.repository.CouponRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RedissonClient redissonClient;

    public CouponResponse createCoupon(CreateCouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new AppCustomException("Mã giảm giá đã tồn tại: " + request.getCode(), HttpStatus.BAD_REQUEST);
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        return mapToResponse(saved, null, null);
    }

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(c -> mapToResponse(c, null, null))
                .collect(Collectors.toList());
    }

    public CouponResponse validateAndCalculateCoupon(ApplyCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCode())
                .orElseThrow(() -> new AppCustomException("Mã giảm giá không hợp lệ", HttpStatus.NOT_FOUND));

        validateCouponCondition(coupon, request.getOrderTotal());

        float calculatedDiscount = calculateDiscountAmount(coupon, request.getOrderTotal());
        float finalTotal = Math.max(0, request.getOrderTotal() - calculatedDiscount);

        return mapToResponse(coupon, calculatedDiscount, finalTotal);
    }

    /**
     * Deduct coupon usage count safely using Redisson Distributed Lock
     */
    @Transactional
    public float applyAndDeductCouponWithLock(String couponCode, float orderTotal) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return 0f;
        }

        String lockKey = "lock:coupon:" + couponCode.toUpperCase();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try acquiring lock for 5 seconds, lease for 10 seconds
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppCustomException("Hệ thống đang bận, vui lòng thử lại!", HttpStatus.TOO_MANY_REQUESTS);
            }

            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode)
                    .orElseThrow(() -> new AppCustomException("Mã giảm giá không hợp lệ", HttpStatus.NOT_FOUND));

            validateCouponCondition(coupon, orderTotal);

            float discountAmount = calculateDiscountAmount(coupon, orderTotal);
            coupon.setUsedCount(coupon.getUsedCount() + 1);

            // Auto-deactivate if reached limit
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                coupon.setActive(false);
            }

            couponRepository.save(coupon);
            return discountAmount;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppCustomException("Xảy ra lỗi khi xử lý lock mã giảm giá", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validateCouponCondition(Coupon coupon, float orderTotal) {
        if (!coupon.isActive()) {
            throw new AppCustomException("Mã giảm giá đã ngưng hoạt động", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new AppCustomException("Mã giảm giá chưa đến thời gian sử dụng", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new AppCustomException("Mã giảm giá đã hết hạn sử dụng", HttpStatus.BAD_REQUEST);
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppCustomException("Mã giảm giá đã hết lượt sử dụng", HttpStatus.BAD_REQUEST);
        }

        if (coupon.getMinOrderAmount() != null && orderTotal < coupon.getMinOrderAmount()) {
            throw new AppCustomException("Đơn hàng chưa đạt giá trị tối thiểu " + coupon.getMinOrderAmount() + "đ để dùng mã này", HttpStatus.BAD_REQUEST);
        }
    }

    private float calculateDiscountAmount(Coupon coupon, float orderTotal) {
        float discount = 0f;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderTotal * (coupon.getDiscountValue() / 100f);
            if (coupon.getMaxDiscountAmount() != null && discount > coupon.getMaxDiscountAmount()) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, orderTotal);
    }

    private CouponResponse mapToResponse(Coupon c, Float calculatedDiscount, Float finalTotal) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .usageLimit(c.getUsageLimit())
                .usedCount(c.getUsedCount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .active(c.isActive())
                .calculatedDiscount(calculatedDiscount)
                .finalTotal(finalTotal)
                .build();
    }
}
