package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByProductId(String productId);

    List<Rating> findByUserId(String userId);
}
