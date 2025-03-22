package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsByProductName(String productName);
}