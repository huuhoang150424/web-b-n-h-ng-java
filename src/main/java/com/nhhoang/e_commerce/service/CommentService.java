package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.Comment;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.CommentRepository;
import com.nhhoang.e_commerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
}