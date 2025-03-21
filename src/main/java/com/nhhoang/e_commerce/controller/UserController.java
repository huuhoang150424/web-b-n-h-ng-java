package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.GetAllUserResponse;
import com.nhhoang.e_commerce.dto.response.GetUserResponse;
import com.nhhoang.e_commerce.dto.response.UserProfileResponse;
import com.nhhoang.e_commerce.dto.response.UserResponse;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.UserService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request,
                                        BindingResult bindingResult,
                                        HttpServletRequest httpRequest) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }

            userService.createUser(request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Tạo mới người dùng thành công");

            return ResponseEntity.status(201).body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<?> getAllUser(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }
            Map<String, Object> result = new HashMap<>();
            List<GetAllUserResponse> userData;
            if (page == null && size == null) {
                List<User> allUsers = userService.getAllUsers();
                userData = allUsers.stream().map(this::mapToUserResponse).collect(Collectors.toList());
                result.put("totalItems", userData.size());
                result.put("currentPage", 1);
                result.put("totalPages", 1);
                result.put("pageSize", userData.size());
                result.put("data", userData);
            } else {
                int pageNum = page != null ? page - 1 : 0;
                int pageSize = size != null ? size : 10;
                if (pageSize > 100) pageSize = 100;
                Page<User> userPage = userService.getAllUsersPaginated(pageNum, pageSize);
                userData = userPage.getContent().stream().map(this::mapToUserResponse).collect(Collectors.toList());
                result.put("totalItems", userPage.getTotalElements());
                result.put("currentPage", userPage.getNumber() + 1);
                result.put("totalPages", userPage.getTotalPages());
                result.put("pageSize", userPage.getSize());
                result.put("data", userData);
            }
            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    private GetAllUserResponse mapToUserResponse(User user) {
        GetAllUserResponse response = new GetAllUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setGender(user.getGender());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setAddress(user.getAddress());
        return response;
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            User user = userService.getUserById(currentUser.getId());
            GetUserResponse response = mapToUserProfileResponse(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", response);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Tài khoản không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
    private GetUserResponse mapToUserProfileResponse(User user) {
        GetUserResponse response = new GetUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setGender(user.getGender());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setAddress(user.getAddress());
        response.setPhone(user.getPhone());
        response.setBirthDate(user.getBirthDate());
        return response;
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            userService.deleteUser(id);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xóa người dùng thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Không thể xóa Admin")) {
                return ResponseEntity.status(404).body(new ErrorResponse("Không thể xóa Admin"));
            }
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @Valid @RequestBody UpdateUserRequest request,
                                        BindingResult bindingResult,
                                        HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }

            userService.updateUser(id, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cập nhật người dùng thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Người dùng không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

}