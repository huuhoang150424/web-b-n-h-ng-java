package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByProductId(String productId);

    List<Rating> findByUserId(String userId);

    Optional<Rating> findByUserAndProduct(User user, Product product);

    Page<Rating> findAll(Pageable pageable);
}
