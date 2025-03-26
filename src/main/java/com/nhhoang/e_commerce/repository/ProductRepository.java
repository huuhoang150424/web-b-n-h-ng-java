package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryId(String categoryId);
    boolean existsByProductName(String productName);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.productAttributes pa LEFT JOIN FETCH pa.attribute WHERE p.slug = :slug")
    Optional<Product> findBySlugWithDetails(String slug);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.productAttributes pa " +
            "LEFT JOIN FETCH pa.attribute " +
            "LEFT JOIN FETCH p.ratings r " +
            "LEFT JOIN FETCH r.user " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(String id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.productAttributes pa " +
            "LEFT JOIN FETCH pa.attribute " +
            "ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(PageRequest pageRequest);

    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByProductNameContainingIgnoreCase(@Param("keyword") String keyword);


}