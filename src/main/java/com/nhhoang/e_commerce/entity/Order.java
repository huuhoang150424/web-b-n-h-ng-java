package com.nhhoang.e_commerce.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @Column(length = 36, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "order_code", length = 7, unique = true)
    private String orderCode;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "total_amount")
    private Float totalAmount;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "receiver_name", length = 255)
    private String receiverName;

    @Column(name = "receiver_phone", length = 15)
    private String receiverPhone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.NOT_CONFIRMED;
        }
        if (orderCode == null) {
            //
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("%s %s", orderCode, totalAmount);
    }

    public enum Status {
        CONFIRMED("Xác nhận"),
        NOT_CONFIRMED("Chưa xác nhận");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}