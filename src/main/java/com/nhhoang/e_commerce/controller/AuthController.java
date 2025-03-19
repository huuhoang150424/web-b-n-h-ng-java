package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.AuthService;
import com.nhhoang.e_commerce.security.jwt.JwtUtil;
import com.nhhoang.e_commerce.utils.Api.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

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
}
