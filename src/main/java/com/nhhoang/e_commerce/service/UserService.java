package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updatePhone(String userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        user.setPhone(phone);
        userRepository.save(user);
    }
}