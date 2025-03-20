package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.UpdateProfileRequest;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Cập nhật các trường nếu có trong request
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        return userRepository.save(user);
    }

    public void addAddress(String userId, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        ArrayList<String> updatedAddress = new ArrayList<>(user.getAddress());
        updatedAddress.add(address);
        user.setAddress(updatedAddress);

        userRepository.save(user);
    }
    public void deleteAddress(String userId, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        ArrayList<String> updatedAddress = new ArrayList<>(user.getAddress());
        updatedAddress.remove(address);
        user.setAddress(updatedAddress);

        userRepository.save(user);
    }
}