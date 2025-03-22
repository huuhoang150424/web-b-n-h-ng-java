package com.nhhoang.e_commerce.entity;

import com.nhhoang.e_commerce.utils.JsonListConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @Column(length = 36, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(length = 60, unique = true)
    private String slug;

    @Column(name = "product_name", length = 50)
    private String productName;

    private Float price;

    @Column(name = "thumb_image", length = 300)
    private String thumbImage;

    private Integer stock;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> imageUrls;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductAttribute> productAttributes = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.AVAILABLE;
        if (description == null) description = "";
        if (imageUrls == null) imageUrls = List.of();
        if (slug == null) generateSlug();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void generateSlug() {
        String baseSlug = productName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", "");
        this.slug = baseSlug;
    }

    public enum Status {
        AVAILABLE("Có sẵn"), OUT_OF_STOCK("Hết hàng"), DISCONTINUED("Ngưng bán");
        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}