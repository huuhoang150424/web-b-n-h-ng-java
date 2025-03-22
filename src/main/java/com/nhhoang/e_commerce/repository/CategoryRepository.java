package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String categoryName);
}