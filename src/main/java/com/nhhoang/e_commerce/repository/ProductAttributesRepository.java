package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributesRepository extends JpaRepository<ProductAttribute, String> {
}