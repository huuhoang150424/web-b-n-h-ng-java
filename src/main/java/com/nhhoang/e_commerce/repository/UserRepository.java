package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN u.orders o GROUP BY u ORDER BY COUNT(o) DESC")
    List<User> findTop20ByOrderByOrderCountDesc();

    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}