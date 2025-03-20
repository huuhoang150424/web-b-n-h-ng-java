package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.ChangePhoneRequest;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.UserService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PatchMapping("/change-phone")
    public ResponseEntity<?> changePhone(@Valid @RequestBody ChangePhoneRequest request, HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            System.out.println("User from request: " + (user != null ? user.getEmail() : "null"));
            if (user == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            userService.updatePhone(user.getId(), request.getPhone());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Số điện thoại đã được thay đổi thành công!");
            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
}