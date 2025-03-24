package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributesRepository extends JpaRepository<ProductAttribute, String> {
    List<ProductAttribute> findByAttributeId(String attributeId);

    List<ProductAttribute> findByProductId(String productId);
}