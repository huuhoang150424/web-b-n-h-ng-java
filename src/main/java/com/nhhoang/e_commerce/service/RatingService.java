package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.Rating;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.ProductRepository;
import com.nhhoang.e_commerce.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public String rateProduct(User user, String productId, RatingRequest ratingRequest) {
        logger.info("User {} is rating product {}", user.getId(), productId);

        // Tìm sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));

        // Tìm rating hiện có
        Optional<Rating> existingRating = ratingRepository.findByUserAndProduct(user, product);

        if (existingRating.isPresent()) {
            // Cập nhật rating
            Rating rating = existingRating.get();
            rating.setRating(ratingRequest.getRating());
            ratingRepository.save(rating);
            return "Đánh giá đã được cập nhật thành công.";
        } else {
            // Tạo rating mới
            Rating newRating = new Rating();
            newRating.setUser(user);
            newRating.setProduct(product);
            newRating.setRating(ratingRequest.getRating());
            ratingRepository.save(newRating);
            return "Đánh giá thành công.";
        }
    }
}