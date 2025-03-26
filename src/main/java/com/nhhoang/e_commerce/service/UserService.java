package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.CreateUserRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateProfileRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateUserRequest;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import com.nhhoang.e_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    public void updatePhone(String userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        user.setPhone(phone);
        userRepository.save(user);
    }
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
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

    public void createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setAvatar(request.getAvatar() != null ? request.getAvatar() : "https://res.cloudinary.com/dw3oj3iju/image/upload/v1709749732/chat_app/b1rj7epnhdqo6t7mcu5w.jpg");
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        userRepository.save(user);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    public Page<User> getAllUsersPaginated(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Không thể xóa Admin");
        }

        cartRepository.findByUserId(id).ifPresent(cart -> {
            cart.setUser(null);
            cartRepository.save(cart);
        });

        List<Order> orders = orderRepository.findByUserId(id);
        for (Order order : orders) {
            order.setUser(null);
            orderRepository.save(order);
        }
        List<FavoriteProduct> favoriteProducts = favoriteProductRepository.findByUserId(id);
        for (FavoriteProduct favoriteProduct : favoriteProducts) {
            favoriteProduct.setUser(null);
            favoriteProductRepository.save(favoriteProduct);
        }
        List<OrderHistory> orderHistories = orderHistoryRepository.findByChangeById(id);
        for (OrderHistory orderHistory : orderHistories) {
            orderHistory.setChangeBy(null);
            orderHistoryRepository.save(orderHistory);
        }
        List<Comment> comments = commentRepository.findByUserId(id);
        for (Comment comment : comments) {
            comment.setUser(null);
            commentRepository.save(comment);
        }
        List<Rating> ratings = ratingRepository.findByUserId(id);
        for (Rating rating : ratings) {
            rating.setUser(null);
            ratingRepository.save(rating);
        }

        // Xóa User
        userRepository.delete(user);
    }
    public void updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (request.getName() != null) {
            String normalizedName = normalizeString(request.getName()); // Chuẩn hóa tên
            user.setName(normalizedName);
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
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        userRepository.save(user);
    }

    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replaceAll("[^\\p{ASCII}]", "");
    }
}