package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByProductId(String productId);

    List<Comment> findByUserId(String userId);

    Page<Comment> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    Optional<Comment> findFirstByProductAndUser(Product product, User user);

    Page<Comment> findAll(Pageable pageable);

    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}