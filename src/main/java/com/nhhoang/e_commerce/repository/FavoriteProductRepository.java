package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, String> {
    List<FavoriteProduct> findByProductId(String productId);

    List<FavoriteProduct> findByUserId(String userId);

    boolean existsByUserAndProduct(User user, Product product);

}