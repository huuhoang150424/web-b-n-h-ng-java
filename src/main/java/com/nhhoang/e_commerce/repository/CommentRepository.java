package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByProductId(String productId);

    List<Comment> findByUserId(String userId);
}