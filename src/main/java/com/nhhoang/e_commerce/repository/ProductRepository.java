package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsByProductName(String productName);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.productAttributes pa LEFT JOIN FETCH pa.attribute WHERE p.slug = :slug")
    Optional<Product> findBySlugWithDetails(String slug);

    List<Product> findByCategoryId(String categoryId);
}