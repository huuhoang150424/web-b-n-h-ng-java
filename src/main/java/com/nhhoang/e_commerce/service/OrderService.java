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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nhhoang.e_commerce.config.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

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

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

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
            order.setTotalAmount(0f);
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

                product.setStock(product.getStock() - item.getQuantity());
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

                    if (detail.getProduct() != null) {
                        productResponse.setId(detail.getProduct().getId());
                        productResponse.setProductName(detail.getProduct().getProductName());
                        productResponse.setPrice(detail.getProduct().getPrice());
                        productResponse.setThumbImage(detail.getProduct().getThumbImage());
                        productResponse.setStock(detail.getProduct().getStock());
                    } else {
                        productResponse.setId(null);
                        productResponse.setProductName("Sản phẩm không tồn tại");
                        productResponse.setPrice(detail.getPrice());
                        productResponse.setThumbImage("");
                        productResponse.setStock(0);

                        logger.warn("Order detail ID {} có product null (Order ID: {})",
                                detail.getId(), order.getId());
                    }

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

        OrderHistoryByUserResponse.ProductResponse productResponse = new OrderHistoryByUserResponse.ProductResponse();
        Product product = detail.getProduct();
        if (product != null) {
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
        } else {
            productResponse.setProductName("Sản phẩm không tồn tại");
            productResponse.setPrice(detail.getPrice());
            productResponse.setThumbImage("");
            productResponse.setStock(0);

            logger.warn("Order detail ID {} có product null (Order ID: {})",
                    detail.getId(), detail.getOrder().getId());
        }
        detailResponse.setProduct(productResponse);

        return detailResponse;
    }

    public List<OrderHistoryByUserResponse> getShippedOrders(String userId) {
        List<OrderHistory> shippedOrders = orderHistoryRepository.findByOrderUserIdAndStatusAndEndTimeIsNull(
                userId, OrderHistory.Status.SHIPPED);
        return shippedOrders.stream()
                .map(this::mapToOrderHistoryShipedByUserResponse)
                .collect(Collectors.toList());
    }

    private OrderHistoryByUserResponse mapToOrderHistoryShipedByUserResponse(OrderHistory history) {
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
                    .map(this::mapToOrderDetailShipedResponse)
                    .collect(Collectors.toList());
            orderResponse.setOrderDetails(orderDetails);

            response.setOrder(orderResponse);
        }

        return response;
    }

    private OrderHistoryByUserResponse.OrderDetailResponse mapToOrderDetailShipedResponse(OrderDetail detail) {
        OrderHistoryByUserResponse.OrderDetailResponse detailResponse = new OrderHistoryByUserResponse.OrderDetailResponse();
        detailResponse.setId(detail.getId());
        detailResponse.setQuantity(detail.getQuantity());
        detailResponse.setPrice(detail.getPrice());

        OrderHistoryByUserResponse.ProductResponse productResponse = new OrderHistoryByUserResponse.ProductResponse();

        Product product = detail.getProduct();
        if (product != null) {
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
        } else {
            productResponse.setProductName("Sản phẩm không tồn tại");
            productResponse.setPrice(detail.getPrice());
            productResponse.setThumbImage("");
            productResponse.setStock(0);

            logger.warn("Order detail ID {} có product null (Order ID: {})",
                    detail.getId(), detail.getOrder().getId());
        }

        detailResponse.setProduct(productResponse);

        return detailResponse;
    }


    public List<OrderHistoryByUserResponse> getProcessingOrders(String userId) {
        List<OrderHistory> processingOrders = orderHistoryRepository.findByOrderUserIdAndStatusAndEndTimeIsNull(
                userId, OrderHistory.Status.PROCESSING);
        return processingOrders.stream()
                .map(this::mapToOrderHistoryProcessByUserResponse)
                .collect(Collectors.toList());
    }

    private OrderHistoryByUserResponse mapToOrderHistoryProcessByUserResponse(OrderHistory history) {
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
                    .map(this::mapToOrderDetailProcessResponse)
                    .collect(Collectors.toList());
            orderResponse.setOrderDetails(orderDetails);

            response.setOrder(orderResponse);
        }

        return response;
    }

    private OrderHistoryByUserResponse.OrderDetailResponse mapToOrderDetailProcessResponse(OrderDetail detail) {
        OrderHistoryByUserResponse.OrderDetailResponse detailResponse = new OrderHistoryByUserResponse.OrderDetailResponse();
        detailResponse.setId(detail.getId());
        detailResponse.setQuantity(detail.getQuantity());
        detailResponse.setPrice(detail.getPrice());
        OrderHistoryByUserResponse.ProductResponse productResponse = new OrderHistoryByUserResponse.ProductResponse();

        Product product = detail.getProduct();
        if (product != null) {
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
        } else {
            productResponse.setProductName("Sản phẩm không tồn tại");
            productResponse.setPrice(detail.getPrice());
            productResponse.setThumbImage("");
            productResponse.setStock(0);

            logger.warn("Processing Order: Order detail ID {} có product null (Order ID: {})",
                    detail.getId(), detail.getOrder().getId());
        }
        detailResponse.setProduct(productResponse);

        return detailResponse;
    }


    public List<OrderHistoryByUserResponse> getReceivedOrders(String userId) {
        List<OrderHistory> receivedOrders = orderHistoryRepository.findByOrderUserIdAndStatusAndEndTimeIsNull(
                userId, OrderHistory.Status.RECEIVED);
        return receivedOrders.stream()
                .map(this::mapToOrderHistoryReceivedByUserResponse)
                .collect(Collectors.toList());
    }

    private OrderHistoryByUserResponse mapToOrderHistoryReceivedByUserResponse(OrderHistory history) {
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
                    .map(this::mapToOrderDetailReceivedResponse)
                    .collect(Collectors.toList());
            orderResponse.setOrderDetails(orderDetails);

            response.setOrder(orderResponse);
        }

        return response;
    }

    private OrderHistoryByUserResponse.OrderDetailResponse mapToOrderDetailReceivedResponse(OrderDetail detail) {
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


    public OrderHistoryResponse getOrderHistory(String orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại hoặc bạn không có quyền truy cập"));

        OrderHistoryResponse response = new OrderHistoryResponse();
        response.setId(order.getId());

        List<OrderHistoryResponse.OrderHistoryByOrderResponse> histories = order.getOrderHistories().stream()
                .map(this::mapToOrderHistoryByOrderResponse)
                .collect(Collectors.toList());
        response.setOrderHistories(histories);

        return response;
    }

    private OrderHistoryResponse.OrderHistoryByOrderResponse mapToOrderHistoryByOrderResponse(OrderHistory history) {
        OrderHistoryResponse.OrderHistoryByOrderResponse response = new OrderHistoryResponse.OrderHistoryByOrderResponse();
        response.setId(history.getId());
        response.setStatus(history.getStatus().name());
        response.setChangedAt(history.getChangedAt());
        response.setEndTime(history.getEndTime());
        response.setOrderId(history.getOrder().getId());

        if (history.getChangeBy() != null) {
            OrderHistoryResponse.OrderHistoryByOrderResponse.UserResponse userResponse = new OrderHistoryResponse.OrderHistoryByOrderResponse.UserResponse();
            userResponse.setId(history.getChangeBy().getId());
            userResponse.setName(history.getChangeBy().getName());
            userResponse.setEmail(history.getChangeBy().getEmail());
            response.setChangeBy(userResponse);
        }

        return response;
    }

    public List<ListOrderChangeResponse> getOrderChanges(String orderId) {
        List<OrderHistory> orderHistories = orderHistoryRepository.findByOrderId(orderId);
        return orderHistories.stream()
                .map(this::mapToListOrderChangeResponse)
                .collect(Collectors.toList());
    }

    private ListOrderChangeResponse mapToListOrderChangeResponse(OrderHistory history) {
        ListOrderChangeResponse response = new ListOrderChangeResponse();
        response.setId(history.getId());
        response.setStatus(history.getStatus().getDisplayName());
        response.setChangedAt(history.getChangedAt());

        if (history.getChangeBy() != null) {
            ListOrderChangeResponse.UserResponse userResponse = new ListOrderChangeResponse.UserResponse();
            userResponse.setName(history.getChangeBy().getName());
            userResponse.setAvatar(history.getChangeBy().getAvatar());
            response.setChangeBy(userResponse);
        }
        return response;
    }

    public List<MonthlyRevenueResponse> getRevenueForYear(Integer year) {
        List<MonthlyRevenueResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlyRevenueResponse monthlyRevenue = new MonthlyRevenueResponse();
            monthlyRevenue.setName("T" + month);
            monthlyRevenue.setTotal(0.0f);
            result.add(monthlyRevenue);
        }
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfYear, endOfYear);
        for (Order order : orders) {
            int month = order.getCreatedAt().getMonthValue();
            result.get(month - 1).setTotal(result.get(month - 1).getTotal() + order.getTotalAmount());
        }

        return result;
    }


    public MonthlyTargetResponse getMonthlyTargets() {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        LocalDateTime startOfMonth = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0);
        LocalDateTime endOfMonth = YearMonth.of(currentYear, currentMonth).atEndOfMonth().atTime(23, 59, 59);

        Integer userCount = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
        Integer orderCount = orderRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
        Integer commentCount = commentRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
        Float totalRevenue = orderRepository.sumTotalAmountByCreatedAtBetween(startOfMonth, endOfMonth);

        MonthlyTargetResponse response = new MonthlyTargetResponse();
        response.setUserCount(userCount);
        response.setOrderCount(orderCount);
        response.setCommentCount(commentCount);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0f);

        return response;
    }

    @Transactional
    public Order createVnPayOrder(String userId, OrderRequestVnPay orderRequest) {
        List<RLock> productLocks = new ArrayList<>();

        try {
            List<CartRequestVnPay> cartItems = orderRequest.getCarts();
            if (cartItems == null || cartItems.isEmpty()) {
                throw new IllegalArgumentException("Giỏ hàng của bạn trống.");
            }

            float totalAmount = 0;
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng với ID " + userId + " không tồn tại.")));
            order.setTotalAmount(0f);
            order.setStatus(Order.Status.CONFIRMED);
            order.setShippingAddress(orderRequest.getShippingAddress());
            order.setReceiverName(orderRequest.getReceiverName());
            order.setReceiverPhone(orderRequest.getReceiverPhone());
            order.setOrderCode(generateOrderCodeVnPay());
            orderRepository.save(order);
            for (CartRequestVnPay item : cartItems) {
                String productId = item.getProduct().getId();
                RLock lock = redissonClient.getLock("lock:product:" + productId);
                if (!lock.tryLock()) {
                    throw new IllegalArgumentException("Sản phẩm " + productId + " đang được mua bởi người khác.");
                }
                productLocks.add(lock);
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm với ID " + productId + " không tồn tại."));
                logger.info("Product stock from repository: {}, Requested quantity: {}", product.getStock(), item.getQuantity());
                if (product.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm " + product.getProductName() + " không đủ số lượng.");
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
                totalAmount += item.getQuantity() * item.getProduct().getPrice();
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(UUID.randomUUID().toString());
                orderDetail.setOrder(order);
                orderDetail.setProduct(product);
                orderDetail.setPrice(item.getProduct().getPrice() * item.getQuantity());
                orderDetail.setQuantity(item.getQuantity());
                orderDetailRepository.save(orderDetail);
                if (item.getId() != null) {
                    CartItem cartItem = cartItemRepository.findById(item.getId())
                            .orElse(null);

                    if (cartItem != null) {
                        int remainingQuantity = cartItem.getQuantity() - item.getQuantity();
                        if (remainingQuantity > 0) {
                            cartItem.setQuantity(remainingQuantity);
                            cartItemRepository.save(cartItem);
                        } else {
                            cartItemRepository.delete(cartItem);
                        }
                    }
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
            payment.setPaymentMethod(Payment.PaymentMethod.VNPAY);
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
            for (RLock lock : productLocks) {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            return order;
        } catch (Exception e) {
            for (RLock lock : productLocks) {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            throw e;
        }
    }

    public void updateOrderPaymentStatus(String orderId, String transactionId, Payment.PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus(paymentStatus);
        paymentRepository.save(payment);
    }

    private String generateOrderCodeVnPay() {
        Random random = new Random();
        return String.format("ORD%04d", random.nextInt(10000));
    }


    public VNPayResponse createPaymentUrl(String orderId, Float amount, String ipAddr) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);

        long amountInCents = Math.round(amount * 100);
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        String orderInfo = "Thanh toan don hang " + orderId;
        try {
            vnpParams.put("vnp_OrderInfo", URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang");
            logger.error("Error encoding vnp_OrderInfo: {}", e.getMessage());
        }

        vnpParams.put("vnp_OrderType", "250000");
        vnpParams.put("vnp_Locale", "vn");

        String returnUrl = "http://localhost:8080/api/order/return";
        try {
            vnpParams.put("vnp_ReturnUrl", URLEncoder.encode(returnUrl, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            logger.error("Error encoding vnp_ReturnUrl: {}", e.getMessage());
        }

        if ("0:0:0:0:0:0:0:1".equals(ipAddr)) {
            ipAddr = "127.0.0.1";
        }
        vnpParams.put("vnp_IpAddr", ipAddr);

        String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpParams.put("vnp_CreateDate", createDate);

        logger.info("VNPay request params (before hash): {}", vnpParams);

        String queryString = VNPayUtil.getQueryString(vnpParams);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpHashSecret, queryString);
        queryString += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnpPayUrl + "?" + queryString;
        logger.info("Generated VNPay payment URL: {}", paymentUrl);

        VNPayResponse response = new VNPayResponse();
        response.setPaymentUrl(paymentUrl);
        response.setStatus("00");
        response.setMessage("Tạo URL thanh toán thành công");
        return response;
    }

    public static String getQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return query.toString();
    }

    public VNPayResponse verifyPaymentReturn(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        logger.info("📌 VNPay Params trước khi hash: {}", params);
        String queryString = VNPayUtil.getQueryString(new TreeMap<>(params));
        logger.info("🔍 Query String trước khi hash: {}", queryString);
        String calculatedHash = VNPayUtil.hmacSHA512(vnpHashSecret, queryString);
        logger.info("🔍 Secure Hash VNPay gửi: {}", vnpSecureHash);
        logger.info("🔍 Secure Hash tính toán được: {}", calculatedHash);
        VNPayResponse response = new VNPayResponse();
        if (calculatedHash.equals(vnpSecureHash)) {
            String responseCode = params.get("vnp_ResponseCode");
            logger.info("✅ VNPay Response Code: {}", responseCode);

            if ("00".equals(responseCode)) {
                response.setStatus("00");
                response.setMessage("Thanh toán thành công");
            } else {
                response.setStatus(responseCode);
                response.setMessage("Thanh toán thất bại: " + params.get("vnp_Message"));
            }
        } else {
            response.setStatus("97");
            response.setMessage("❌ Chữ ký không hợp lệ");
            logger.warn("❌ Chữ ký không hợp lệ! Kiểm tra lại secret key hoặc query string.");
        }

        return response;
    }
}