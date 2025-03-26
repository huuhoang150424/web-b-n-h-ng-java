package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, String> {
    List<FavoriteProduct> findByProductId(String productId);

    List<FavoriteProduct> findByUserId(String userId);

    boolean existsByUserAndProduct(User user, Product product);

    Optional<FavoriteProduct> findByUserIdAndProductId(String userId, String productId);
    boolean existsByUserIdAndProductId(String userId, String productId);

    @Query("SELECT DISTINCT fp FROM FavoriteProduct fp " +
            "LEFT JOIN FETCH fp.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.productAttributes pa " +
            "LEFT JOIN FETCH pa.attribute " +
            "WHERE fp.user.id = :userId")
    List<FavoriteProduct> findByUserIds(String userId);
}