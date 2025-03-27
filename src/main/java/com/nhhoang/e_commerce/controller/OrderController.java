package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.OrderService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/cod")
    public ResponseEntity<?> createOrderCod(HttpServletRequest request,
                                            @RequestBody CreateOrderCodRequest orderRequest) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for create order COD request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            String orderId = orderService.createOrderCod(currentUser.getId(), orderRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Tạo đơn hàng và thanh toán COD thành công");
            result.put("order_id", orderId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponse("Tạo đơn hàng và thanh toán COD thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating order COD: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating order COD: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}