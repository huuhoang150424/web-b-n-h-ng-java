package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Transactional
    public String createOrderCod(String userId, CreateOrderCodRequest request) {
        List<RLock> productLocks = new ArrayList<>();

        try {
            List<CreateOrderCodRequest.CartItemRequest> cartItems = request.getCarts();
            if (cartItems == null || cartItems.isEmpty()) {
                throw new IllegalArgumentException("Giỏ hàng của bạn trống.");
            }

            float totalAmount = 0;

            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng với ID " + userId + " không tồn tại.")));
            order.setTotalAmount(0f); // Sẽ cập nhật sau
            order.setStatus(Order.Status.NOT_CONFIRMED);
            order.setShippingAddress(request.getShippingAddress());
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setOrderCode(generateOrderCode());
            orderRepository.save(order);

            for (CreateOrderCodRequest.CartItemRequest item : cartItems) {
                String productId = item.getProduct().getId();
                RLock lock = redissonClient.getLock("lock:product:" + productId);
                if (!lock.tryLock()) {
                    throw new IllegalArgumentException("Sản phẩm " + productId + " đang được mua bởi người khác.");
                }
                productLocks.add(lock);

                CartItem cartItem = cartItemRepository.findById(item.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cart item với ID " + item.getId() + " không tồn tại hoặc không thuộc về bạn."));

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm với ID " + productId + " không tồn tại."));

                logger.info("Product stock from repository: {}, Requested quantity: {}", product.getStock(), item.getQuantity());
                if (product.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm " + product.getProductName() + " không đủ số lượng.");
                }

                product.setStock(product.getStock() - item.getQuantity()); // Trừ stock một lần
                productRepository.save(product);
                totalAmount += item.getQuantity() * item.getProduct().getPrice().floatValue();

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(UUID.randomUUID().toString());
                orderDetail.setOrder(order);
                orderDetail.setProduct(product);
                orderDetail.setPrice(item.getProduct().getPrice().floatValue() * item.getQuantity());
                orderDetail.setQuantity(item.getQuantity());
                orderDetailRepository.save(orderDetail);

                int remainingQuantity = cartItem.getQuantity() - item.getQuantity();
                if (remainingQuantity > 0) {
                    cartItem.setQuantity(remainingQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    cartItemRepository.delete(cartItem);
                }
            }

            order.setTotalAmount(totalAmount);
            orderRepository.save(order);

            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setId(UUID.randomUUID().toString());
            orderHistory.setStatus(OrderHistory.Status.PROCESSING);
            orderHistory.setOrder(order);
            orderHistory.setChangeBy(userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng với ID " + userId + " không tồn tại.")));
            orderHistoryRepository.save(orderHistory);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaymentMethod(Payment.PaymentMethod.COD);
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            payment.setTransactionId("N/A");
            paymentRepository.save(payment);

            for (RLock lock : productLocks) {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            return order.getId();
        } catch (Exception e) {
            for (RLock lock : productLocks) {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            throw e;
        }
    }

    private String generateOrderCode() {
        return "ORD" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }


    public PaginatedOrderResponse getAllOrders(Integer page, Integer size) {
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 10;
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;

        if (page == null && size == null) {
            List<Order> allOrders = orderRepository.findAll();
            List<PaginatedOrderResponse.GetOrderResponse> orderResponses = allOrders.stream()
                    .map(this::mapToGetOrderResponse)
                    .collect(Collectors.toList());
            return new PaginatedOrderResponse(
                    orderResponses.size(),
                    1,
                    1,
                    orderResponses.size(),
                    orderResponses
            );
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<PaginatedOrderResponse.GetOrderResponse> orderResponses = orderPage.getContent().stream()
                .map(this::mapToGetOrderResponse)
                .collect(Collectors.toList());

        return new PaginatedOrderResponse(
                (int) orderPage.getTotalElements(),
                orderPage.getNumber() + 1,
                orderPage.getTotalPages(),
                orderPage.getSize(),
                orderResponses
        );
    }

    private PaginatedOrderResponse.GetOrderResponse mapToGetOrderResponse(Order order) {
        PaginatedOrderResponse.GetOrderResponse response = new PaginatedOrderResponse.GetOrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setShippingAddress(order.getShippingAddress());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setOrderCode(order.getOrderCode());

        if (order.getUser() != null) {
            PaginatedOrderResponse.UserOrderResponse userResponse = new PaginatedOrderResponse.UserOrderResponse();
            userResponse.setName(order.getUser().getName());
            userResponse.setEmail(order.getUser().getEmail());
            response.setUser(userResponse);
        } else {
            response.setUser(null);
        }

        return response;
    }

    public List<OrderByUserResponse> getOrdersByUser(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToOrderByUserResponse)
                .collect(Collectors.toList());
    }

    private OrderByUserResponse mapToOrderByUserResponse(Order order) {
        OrderByUserResponse response = new OrderByUserResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setShippingAddress(order.getShippingAddress());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setOrderCode(order.getOrderCode());

        List<OrderByUserResponse.OrderDetailResponse> orderDetails = order.getOrderDetails().stream()
                .map(detail -> {
                    OrderByUserResponse.OrderDetailResponse detailResponse = new OrderByUserResponse.OrderDetailResponse();
                    detailResponse.setId(detail.getId());
                    detailResponse.setQuantity(detail.getQuantity());
                    detailResponse.setPrice(detail.getPrice());

                    OrderByUserResponse.OrderDetailResponse.ProductResponse productResponse = new OrderByUserResponse.OrderDetailResponse.ProductResponse();
                    productResponse.setId(detail.getProduct().getId());
                    productResponse.setProductName(detail.getProduct().getProductName());
                    productResponse.setPrice(detail.getProduct().getPrice());
                    productResponse.setThumbImage(detail.getProduct().getThumbImage());
                    detailResponse.setProduct(productResponse);

                    return detailResponse;
                })
                .collect(Collectors.toList());
        response.setOrderDetails(orderDetails);

        return response;
    }


    @Transactional
    public void confirmOrder(String orderId, User currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (!order.getStatus().equals(Order.Status.NOT_CONFIRMED)) {
            throw new IllegalArgumentException("Đơn hàng không thể xác nhận");
        }

        boolean isCancelled = orderHistoryRepository.existsByOrderIdAndStatus(orderId, OrderHistory.Status.CANCELLED);
        if (isCancelled) {
            throw new IllegalArgumentException("Đơn hàng đã bị hủy không thể xác nhận");
        }

        order.setStatus(Order.Status.CONFIRMED);
        orderRepository.save(order);

        OrderHistory currentHistory = orderHistoryRepository.findFirstByOrderIdAndStatusOrderByChangedAtDesc(
                orderId, OrderHistory.Status.PROCESSING);
        if (currentHistory != null) {
            currentHistory.setEndTime(LocalDateTime.now());
            orderHistoryRepository.save(currentHistory);
        }

        OrderHistory newHistory = new OrderHistory();
        newHistory.setId(UUID.randomUUID().toString());
        newHistory.setStatus(OrderHistory.Status.SHIPPED);
        newHistory.setOrder(order);
        newHistory.setChangeBy(currentUser);
        newHistory.setChangedAt(LocalDateTime.now());
        orderHistoryRepository.save(newHistory);
    }

    @Transactional
    public void cancelOrder(String orderId, User currentUser) {
        Order order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại hoặc bạn không có quyền hủy đơn này"));

        if (order.getStatus().equals(Order.Status.CONFIRMED)) {
            throw new IllegalArgumentException("Đơn hàng đã được xác nhận, không thể hủy");
        }

        boolean isCancelled = orderHistoryRepository.existsByOrderIdAndStatus(orderId, OrderHistory.Status.CANCELLED);
        if (order.getStatus().equals(Order.Status.NOT_CONFIRMED) && isCancelled) {
            throw new IllegalArgumentException("Đơn hàng đã bị hủy trước đó");
        }

        OrderHistory currentHistory = orderHistoryRepository.findFirstByOrderIdAndStatusOrderByChangedAtDesc(
                orderId, OrderHistory.Status.PROCESSING);
        if (currentHistory != null) {
            currentHistory.setEndTime(LocalDateTime.now());
            orderHistoryRepository.save(currentHistory);
        }

        OrderHistory newHistory = new OrderHistory();
        newHistory.setId(UUID.randomUUID().toString());
        newHistory.setStatus(OrderHistory.Status.CANCELLED);
        newHistory.setOrder(order);
        newHistory.setChangeBy(currentUser);
        newHistory.setChangedAt(LocalDateTime.now());
        orderHistoryRepository.save(newHistory);
    }

    @Transactional
    public void receiveOrder(String orderId, User currentUser) {
        Order order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại hoặc bạn không có quyền xác nhận đơn này"));

        if (order.getStatus().equals(Order.Status.NOT_CONFIRMED)) {
            throw new IllegalArgumentException("Đơn hàng chưa được xác nhận");
        }

        boolean isShipped = orderHistoryRepository.existsByOrderIdAndStatus(orderId, OrderHistory.Status.SHIPPED);
        if (!isShipped) {
            throw new IllegalArgumentException("Đơn hàng chưa được giao");
        }

        boolean isReceived = orderHistoryRepository.existsByOrderIdAndStatus(orderId, OrderHistory.Status.RECEIVED);
        if (isReceived) {
            throw new IllegalArgumentException("Đơn hàng đã được xác nhận là 'Đã nhận hàng' trước đó");
        }

        OrderHistory currentHistory = orderHistoryRepository.findFirstByOrderIdAndStatusOrderByChangedAtDesc(
                orderId, OrderHistory.Status.SHIPPED);
        if (currentHistory != null) {
            currentHistory.setEndTime(LocalDateTime.now());
            orderHistoryRepository.save(currentHistory);
        }

        OrderHistory newHistory = new OrderHistory();
        newHistory.setId(UUID.randomUUID().toString());
        newHistory.setStatus(OrderHistory.Status.RECEIVED);
        newHistory.setOrder(order);
        newHistory.setChangeBy(currentUser);
        newHistory.setChangedAt(LocalDateTime.now());
        orderHistoryRepository.save(newHistory);
    }

    public List<OrderHistoryByUserResponse> getCancelledOrders(String userId) {
        List<OrderHistory> cancelledOrders = orderHistoryRepository.findByOrderUserIdAndStatusAndEndTimeIsNull(
                userId, OrderHistory.Status.CANCELLED);
        return cancelledOrders.stream()
                .map(this::mapToOrderHistoryByUserResponse)
                .collect(Collectors.toList());
    }

    private OrderHistoryByUserResponse mapToOrderHistoryByUserResponse(OrderHistory history) {
        OrderHistoryByUserResponse response = new OrderHistoryByUserResponse();
        response.setId(history.getId());
        response.setStatus(history.getStatus().name());
        response.setChangedAt(history.getChangedAt());
        response.setEndTime(history.getEndTime());

        Order order = history.getOrder();
        if (order != null) {
            OrderHistoryByUserResponse.OrderResponse orderResponse = new OrderHistoryByUserResponse.OrderResponse();
            orderResponse.setId(order.getId());
            orderResponse.setTotalAmount(order.getTotalAmount());
            orderResponse.setStatus(order.getStatus().name());
            orderResponse.setCreatedAt(order.getCreatedAt());
            orderResponse.setShippingAddress(order.getShippingAddress());
            orderResponse.setReceiverName(order.getReceiverName());
            orderResponse.setReceiverPhone(order.getReceiverPhone());
            orderResponse.setOrderCode(order.getOrderCode());

            List<OrderHistoryByUserResponse.OrderDetailResponse> orderDetails = order.getOrderDetails().stream()
                    .map(this::mapToOrderDetailResponse)
                    .collect(Collectors.toList());
            orderResponse.setOrderDetails(orderDetails);

            response.setOrder(orderResponse);
        }

        return response;
    }

    private OrderHistoryByUserResponse.OrderDetailResponse mapToOrderDetailResponse(OrderDetail detail) {
        OrderHistoryByUserResponse.OrderDetailResponse detailResponse = new OrderHistoryByUserResponse.OrderDetailResponse();
        detailResponse.setId(detail.getId());
        detailResponse.setQuantity(detail.getQuantity());
        detailResponse.setPrice(detail.getPrice());

        Product product = detail.getProduct();
        if (product != null) {
            OrderHistoryByUserResponse.ProductResponse productResponse = new OrderHistoryByUserResponse.ProductResponse();
            productResponse.setProductName(product.getProductName());
            productResponse.setPrice(product.getPrice());
            productResponse.setThumbImage(product.getThumbImage());
            productResponse.setStock(product.getStock());

            if (product.getCategory() != null) {
                OrderHistoryByUserResponse.CategoryResponse categoryResponse = new OrderHistoryByUserResponse.CategoryResponse();
                categoryResponse.setId(product.getCategory().getId());
                categoryResponse.setCategoryName(product.getCategory().getCategoryName());
                categoryResponse.setImage(product.getCategory().getImage());
                productResponse.setCategory(categoryResponse);
            }

            detailResponse.setProduct(productResponse);
        }

        return detailResponse;
    }

}