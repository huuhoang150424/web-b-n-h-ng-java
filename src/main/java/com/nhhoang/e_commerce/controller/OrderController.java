package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.service.OrderService;
import com.nhhoang.e_commerce.utils.Api.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders(
            HttpServletRequest request,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            User currentUser = (User) request.getAttribute("user");

            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }
            PaginatedOrderResponse paginatedOrders = orderService.getAllOrders(page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Lấy danh sách đơn hàng thành công");
            result.put("orders", paginatedOrders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching all orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching all orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderByUser")
    public ResponseEntity<?> getOrderByUser(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get orders by user request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<OrderByUserResponse> orders = orderService.getOrdersByUser(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Lấy danh sách đơn hàng của người dùng thành công");
            result.put("data", orders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching orders by user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching orders by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }


    @PutMapping("/confirmOrder/{id}")
    public ResponseEntity<?> confirmOrder(HttpServletRequest request, @PathVariable String id) {
        try {
            User currentUser = (User) request.getAttribute("user");

            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            orderService.confirmOrder(id, currentUser);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xác nhận đơn hàng thành công");

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error confirming order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error confirming order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PutMapping("/destroy/{id}")
    public ResponseEntity<?> destroyOrders(HttpServletRequest request, @PathVariable String id) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for cancel order request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            orderService.cancelOrder(id, currentUser);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Hủy đơn hàng thành công");

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PutMapping("/received/{id}")
    public ResponseEntity<?> receivedOrder(HttpServletRequest request, @PathVariable String id) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for receive order request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            orderService.receiveOrder(id, currentUser);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xác nhận đã nhận hàng thành công");

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error receiving order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error receiving order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderDestroy")
    public ResponseEntity<?> getOrderDestroy(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get cancelled orders request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<OrderHistoryByUserResponse> cancelledOrders = orderService.getCancelledOrders(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", cancelledOrders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching cancelled orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching cancelled orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderShipped")
    public ResponseEntity<?> getOrderShipped(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get shipped orders request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<OrderHistoryByUserResponse> shippedOrders = orderService.getShippedOrders(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", shippedOrders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching shipped orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching shipped orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderProcessing")
    public ResponseEntity<?> getOrderProcessing(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get processing orders request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<OrderHistoryByUserResponse> processingOrders = orderService.getProcessingOrders(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", processingOrders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching processing orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching processing orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderReceived")
    public ResponseEntity<?> getOrderReceived(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get received orders request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<OrderHistoryByUserResponse> receivedOrders = orderService.getReceivedOrders(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", receivedOrders);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching received orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching received orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getOrderHistory/{id}")
    public ResponseEntity<?> getOrderHistory(HttpServletRequest request, @PathVariable String id) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get order history request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            OrderHistoryResponse orderHistory = orderService.getOrderHistory(id, currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", orderHistory);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching order history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching order history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getListOrderChange/{id}")
    public ResponseEntity<?> getListOrderChange(HttpServletRequest request, @PathVariable String id) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get order changes request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }


            List<ListOrderChangeResponse> orderChanges = orderService.getOrderChanges(id);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", orderChanges);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching order changes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching order changes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/revenue/{year}")
    public ResponseEntity<?> getRevenueForTheYear(HttpServletRequest request, @PathVariable Integer year) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get revenue request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            List<MonthlyRevenueResponse> revenueData = orderService.getRevenueForYear(year);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", revenueData);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching revenue for year {}: {}", year, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getTargetForTheMonth")
    public ResponseEntity<?> getTargetForTheMonth() {
        try {
            MonthlyTargetResponse targets = orderService.getMonthlyTargets();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", targets);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching monthly targets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}