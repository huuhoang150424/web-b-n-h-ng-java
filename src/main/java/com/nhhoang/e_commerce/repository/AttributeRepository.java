package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Attributes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attributes, String> {
    boolean existsByAttributeName(String attributeName);
}