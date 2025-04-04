package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.RatingService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class RatingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);

    @Autowired
    private RatingService ratingService;

    @PostMapping("/ratingProduct/{productId}")
    public ResponseEntity<?> rateProduct(
            HttpServletRequest request,
            @PathVariable String productId,
            @Valid @RequestBody RatingRequest ratingRequest) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for rating request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            String message = ratingService.rateProduct(currentUser, productId, ratingRequest);
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);

            HttpStatus status = message.contains("cập nhật") ? HttpStatus.OK : HttpStatus.CREATED;
            return ResponseEntity.status(status)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllReview")
    public ResponseEntity<?> getProductReviews(
            HttpServletRequest request,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, name = "size") Integer size) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get reviews request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (currentUser.getRole() != Role.ADMIN) {
                logger.warn("User {} is not authorized to access reviews", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Chỉ admin mới có quyền truy cập"));
            }
            RatingService.ReviewResult result = ratingService.getProductReviews(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("totalItems", result.getTotalItems());
            response.put("currentPage", result.getCurrentPage());
            response.put("totalPages", result.getTotalPages());
            response.put("pageSize", result.getPageSize());
            response.put("data", result.getData());

            return ResponseEntity.ok(new SuccessResponse("Thành công", response));
        } catch (Exception e) {
            logger.error("Error fetching product reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}