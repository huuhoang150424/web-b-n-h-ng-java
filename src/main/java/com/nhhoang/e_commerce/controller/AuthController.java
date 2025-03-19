package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.AuthService;
import com.nhhoang.e_commerce.security.jwt.JwtUtil;
import com.nhhoang.e_commerce.utils.Api.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    //login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserResponse userResponse = authService.login(request);
            User user = authService.findById(userResponse.getId());
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .maxAge(365 * 24 * 60 * 60)
                    .path("/")
                    .build();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("user", userResponse);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new SuccessResponse("Đăng nhập thành công", responseData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    //register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequests request) {
        try {
            authService.register(request);
            return ResponseEntity.status(201).body(new SuccessResponse("Đăng ký thành công",null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    //logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .body(new SuccessResponse("Đăng xuất thành công",null));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = null;
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        System.out.println("Refresh Token: " + refreshToken);

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            String userId = jwtUtil.extractUserId(refreshToken);
            User user = authService.findById(userId);
            if (user != null) {
                String accessToken = jwtUtil.generateAccessToken(user);
                Map<String, String> responseData = new HashMap<>();
                responseData.put("refreshToken", refreshToken); // Giữ nguyên refreshToken
                responseData.put("accessToken", accessToken);
                return ResponseEntity.ok(responseData);
            }
        }

        return ResponseEntity.status(401).body(new ErrorResponse("Refresh token không hợp lệ"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user != null) {
            UserResponse userResponse = authService.mapToUserResponse(user);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", userResponse);
            return ResponseEntity.ok(new SuccessResponse("Thông tin user", responseData));
        }
        return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            User user = authService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Người dùng không tồn tại"));
            }

            String otpCode = String.format("%04d", new Random().nextInt(10000));
            String redisKey = "otp:" + request.getEmail();

            redisTemplate.opsForValue().set(redisKey, otpCode, 300, TimeUnit.SECONDS);

            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            Instant expirationTime = ttl > 0 ? Instant.now().plus(ttl, ChronoUnit.SECONDS) : null;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Mã xác thực (OTP)");
            message.setText(String.format("Chào %s,\n\nMã xác thực của bạn là: %s\nMã có hiệu lực trong 5 phút.",
                    user.getName(), otpCode));
            message.setFrom("your-email@gmail.com");
            mailSender.send(message);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "Mã OTP đã được gửi qua email.");
            data.put("expiration", expirationTime != null ? expirationTime.toString() : null);
            data.put("email", request.getEmail());

            return ResponseEntity.ok(new SuccessResponse("Gửi mail thành công", data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
}
