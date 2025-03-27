package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
}