package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.FavoriteProductRequest;
import com.nhhoang.e_commerce.entity.FavoriteProduct;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.FavoriteProductService;
import com.nhhoang.e_commerce.utils.Api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class FavoriteProductController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteProductController.class);

    @Autowired
    private FavoriteProductService favoriteProductService;

    @PostMapping("/addFavoriteProduct")
    public ResponseEntity<?> addFavoriteProduct(HttpServletRequest httpRequest,
                                                @Valid @RequestBody FavoriteProductRequest request) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for addFavoriteProduct request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            FavoriteProduct favoriteProduct = favoriteProductService.addFavoriteProduct(currentUser, request);

            Map<String, Object> result = new HashMap<>();
            result.put("favoriteProductId", favoriteProduct.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponse("Thêm sản phẩm yêu thích thành công", result));
        } catch (IllegalStateException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding favorite product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/removeFavoriteProduct")
    public ResponseEntity<?> removeFavoriteProduct(HttpServletRequest httpRequest,
                                                   @Valid @RequestBody FavoriteProductRequest request) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for removeFavoriteProduct request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            favoriteProductService.removeFavoriteProduct(currentUser, request);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xóa sản phẩm yêu thích thành công");

            return ResponseEntity.ok(new SuccessResponse("Xóa sản phẩm yêu thích thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Favorite product not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing favorite product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }
}