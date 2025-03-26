package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.Comment;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.CommentRepository;
import com.nhhoang.e_commerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public String commentOnProduct(User user, String productId, CommentRequest commentRequest) {
        logger.info("User {} is commenting on product {}", user.getId(), productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));

        Comment comment = new Comment();
        comment.setComment(commentRequest.getComment());
        comment.setUser(user);
        comment.setProduct(product);

        commentRepository.save(comment);
        return "Bình luận thành công.";
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByProduct(String productId, int offset, int limit) {
        logger.info("Fetching comments for product {} with offset {} and limit {}", productId, offset, limit);

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Comment> commentPage = commentRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);

        return commentPage.map(this::mapToCommentResponse);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setComment(comment.getComment());
        response.setCreatedAt(comment.getCreatedAt());

        UserInfoCommentResponse userInfo = new UserInfoCommentResponse();
        userInfo.setId(comment.getUser().getId());
        userInfo.setName(comment.getUser().getName());
        userInfo.setEmail(comment.getUser().getEmail());
        userInfo.setAvatar(comment.getUser().getAvatar());
        response.setUserInfo(userInfo);

        return response;
    }
}