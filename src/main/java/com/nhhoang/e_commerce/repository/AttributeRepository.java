package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Attributes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attributes, String> {
    boolean existsByAttributeName(String attributeName);
    Optional<Attributes> findByAttributeName(String attributeName); // Thêm phương thức này
}