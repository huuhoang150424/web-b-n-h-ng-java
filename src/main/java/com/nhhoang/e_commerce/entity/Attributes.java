package com.nhhoang.e_commerce.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attributes")
@Data
public class Attributes {

    @Id
    @Column(length = 36, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "attribute_name", length = 50)
    private String attributeName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<ProductAttribute> productAttributes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return attributeName;
    }
}