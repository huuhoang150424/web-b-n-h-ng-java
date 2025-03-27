package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, String> {
    List<OrderHistory> findByChangeById(String changeById);

    boolean existsByOrderIdAndStatus(String orderId, OrderHistory.Status status);

    OrderHistory findFirstByOrderIdAndStatusOrderByChangedAtDesc(String orderId, OrderHistory.Status status);
}