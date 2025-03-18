package com.nhhoang.e_commerce.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_history")
@Data
public class OrderHistory {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "change_by")
    private User changeBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = Status.PROCESSING;
        }
        changedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        changedAt = LocalDateTime.now();
    }
    @Override
    public String toString() {
        return String.format("%s %s %s", id, status, order);
    }
    public enum Status {
        PROCESSING("Đang xử lý"),
        SHIPPED("Đang giao"),
        CANCELLED("Hủy"),
        RECEIVED("Đã nhận hàng");
        private final String displayName;
        Status(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
}