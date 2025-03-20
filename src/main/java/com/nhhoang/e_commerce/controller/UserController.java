package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.AddAddressRequest;
import com.nhhoang.e_commerce.dto.requests.ChangePhoneRequest;
import com.nhhoang.e_commerce.dto.requests.DeleteAddressRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateProfileRequest;
import com.nhhoang.e_commerce.dto.response.UserProfileResponse;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.UserService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                           BindingResult bindingResult,
                                           HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            System.out.println("User from request: " + (user != null ? user.getEmail() : "null"));
            if (user == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }

            // Cập nhật profile
            User updatedUser = userService.updateProfile(user.getId(), request);

            // Tạo response data
            UserProfileResponse responseData = new UserProfileResponse();
            responseData.setName(updatedUser.getName());
            responseData.setEmail(updatedUser.getEmail());
            responseData.setGender(updatedUser.getGender());
            responseData.setAvatar(updatedUser.getAvatar());
            responseData.setBirthDate(updatedUser.getBirthDate());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cập nhật thông tin người dùng thành công");
            result.put("data", responseData);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @PatchMapping("/add-address")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddAddressRequest request,
                                        BindingResult bindingResult,
                                        HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            System.out.println("User from request: " + (user != null ? user.getEmail() : "null"));
            if (user == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }
            if (user.getAddress().contains(request.getAddress())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Địa chỉ này đã tồn tại"));
            }
            userService.addAddress(user.getId(), request.getAddress());
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thêm mới địa chỉ thành công");
            result.put("updated_address_list", user.getAddress());

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @PatchMapping("/delete-address")
    public ResponseEntity<?> deleteAddress(@Valid @RequestBody DeleteAddressRequest request,
                                           BindingResult bindingResult,
                                           HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            System.out.println("User from request: " + (user != null ? user.getEmail() : "null"));
            if (user == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }
            if (!user.getAddress().contains(request.getAddress())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Địa chỉ không tồn tại trong danh sách"));
            }
            userService.deleteAddress(user.getId(), request.getAddress());
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xóa địa chỉ thành công");
            result.put("updated_address_list", user.getAddress());

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
}