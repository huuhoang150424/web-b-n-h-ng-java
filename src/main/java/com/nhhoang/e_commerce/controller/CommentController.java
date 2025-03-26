package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.CommentService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 30;

    @Autowired
    private CommentService commentService;

    @PostMapping("/comment/{productId}")
    public ResponseEntity<?> commentOnProduct(
            HttpServletRequest request,
            @PathVariable String productId,
            @Valid @RequestBody CommentRequest commentRequest) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for comment request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            String message = commentService.commentOnProduct(currentUser, productId, commentRequest);
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error commenting on product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getCommentProduct/{productId}")
    public ResponseEntity<?> getCommentsByProduct(
            HttpServletRequest request,
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get comments request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            if (limit <= 0 || limit > MAX_LIMIT) {
                limit = DEFAULT_LIMIT;
            }

            Page<CommentResponse> commentPage = commentService.getCommentsByProduct(productId, offset, limit);

            Map<String, Object> result = new HashMap<>();
            result.put("results", commentPage.getContent());
            result.put("has_next", commentPage.hasNext());
            result.put("next_offset", commentPage.hasNext() ? offset + limit : null);
            result.put("count", commentPage.getTotalElements());

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching comments for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}