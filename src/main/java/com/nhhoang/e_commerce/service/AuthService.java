package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.LoginRequest;
import com.nhhoang.e_commerce.dto.requests.RegisterRequests;
import com.nhhoang.e_commerce.dto.response.UserResponse;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    //login
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        return mapToUserResponse(user);
    }
    //register
    public void register(RegisterRequests request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu không chính xác");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }


    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setAddress(user.getAddress());
        response.setBirthDate(user.getBirthDate());
        response.setGender(user.getGender().name());
        response.setPhone(user.getPhone());
        return response;
    }
}