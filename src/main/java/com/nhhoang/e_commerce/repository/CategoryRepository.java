package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByCategoryName(String categoryName);

    @Query("SELECT c FROM Category c LEFT JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Category> findTop5ByOrderByProductCountDesc();
}