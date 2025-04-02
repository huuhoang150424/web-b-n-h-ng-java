package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(String userId);

    Optional<Order> findByIdAndUserId(String id, String userId);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Float sumTotalAmountByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}