package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.AuthService;
import com.nhhoang.e_commerce.security.jwt.JwtUtil;
import com.nhhoang.e_commerce.utils.Api.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import javax.security.sasl.AuthenticationException;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserResponse userResponse = authService.login(request);
            User user = authService.findById(userResponse.getId());
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("user", userResponse);


            return ResponseEntity.ok()
                    .body(new SuccessResponse("Đăng nhập thành công", result));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Sai tài khoản hoặc mật khẩu"));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Tài khoản của bạn đã bị vô hiệu hóa"));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Tài khoản của bạn đã bị khóa"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse("Lỗi không xác định: " + e.getMessage()));
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
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .body(new SuccessResponse("Đăng xuất thành công",null));
    }

//    @GetMapping("/refresh-token")
//    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
//        try {
//            String refreshToken = getRefreshTokenFromCookies(request);
//            System.out.println("check refreshToken"+refreshToken);
//            if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Refresh token không hợp lệ"));
//            }
//            String userId = jwtUtil.extractUserId(refreshToken);
//            User user = authService.findById(userId);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Người dùng không tồn tại"));
//            }
//            String accessToken = jwtUtil.generateAccessToken(user.getId(),user.getRole());
//            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
//            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newRefreshToken)
//                    .httpOnly(true)
//                    .secure(false)
//                    .sameSite("Lax")
//                    .maxAge(365 * 24 * 60 * 60)
//                    .path("/")
//                    .build();
//            Map<String, Object> responseData = new HashMap<>();
//
//            responseData.put("accessToken", accessToken);
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
//                    .body(new SuccessResponse("Đăng nhập thành công", responseData));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Lỗi khi làm mới token"));
//        }
//    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        System.out.println("user "+user);
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

    //change password
    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Mật khẩu cũ không đúng"));
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Mật khẩu xác nhận không khớp"));
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            authService.updateUser(user);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("message", "Mật khẩu đã được thay đổi thành công!");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    //verify-code
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            String storedOtp = redisTemplate.opsForValue().get("otp:" + request.getEmail());
            if (storedOtp == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Mã OTP đã hết hạn hoặc không tồn tại"));
            }
            if (!storedOtp.equals(request.getOtpCode())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Mã OTP không hợp lệ"));
            }
            User user = authService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email không tồn tại trong hệ thống"));
            }
            redisTemplate.delete("otp:" + request.getEmail());
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Mã OTP hợp lệ và đã được xác thực.");
            return ResponseEntity.ok(new SuccessResponse("Xác thực thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
    //reset-password
    @PatchMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            User user = authService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Tài khoản không tồn tại"));
            }
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Xác nhận mật khẩu không chính xác"));
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            authService.updateUser(user);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

}
