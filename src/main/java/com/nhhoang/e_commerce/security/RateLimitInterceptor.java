package com.nhhoang.e_commerce.security;

import com.nhhoang.e_commerce.exception.AppCustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long TIME_WINDOW_SECONDS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String requestURI = request.getRequestURI();

        // Rate limit sensitive endpoints (Auth OTP, Register, Forgot Password, Validate Coupon)
        if (isRateLimitedEndpoint(requestURI)) {
            String redisKey = "rate_limit:" + clientIp + ":" + requestURI;
            Long count = redisTemplate.opsForValue().increment(redisKey);

            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
            }

            if (count != null && count > MAX_REQUESTS_PER_MINUTE) {
                Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                throw new AppCustomException(
                        String.format("Bạn thao tác quá nhanh! Vượt quá giới hạn %d lượt/phút. Vui lòng thử lại sau %d giây.",
                                MAX_REQUESTS_PER_MINUTE, ttl != null ? ttl : TIME_WINDOW_SECONDS),
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }
        return true;
    }

    private boolean isRateLimitedEndpoint(String uri) {
        return uri.contains("/api/auth/register") ||
               uri.contains("/api/auth/forgot-password") ||
               uri.contains("/api/auth/verify-code") ||
               uri.contains("/api/coupons/validate");
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
