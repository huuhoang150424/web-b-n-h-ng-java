package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public String addToCart(String userId, AddCartRequest request) {
        logger.info("Adding product to cart for user: {}, productId: {}, quantity: {}", userId, request.getProductId(), request.getQuantity());

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Không đủ số lượng sản phẩm trong kho");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(new com.nhhoang.e_commerce.entity.User());
                    newCart.getUser().setId(userId);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        if (cartItem.getId() != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException("Số lượng sản phẩm không đủ");
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            return "Sản phẩm đã được thêm vào giỏ hàng";
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return cart.getCartItems().isEmpty() ?
                "Giỏ hàng đã được tạo và sản phẩm đã được thêm thành công" :
                "Sản phẩm đã được thêm vào giỏ hàng thành công";
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        logger.info("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Giỏ hàng không tồn tại cho người dùng: " + userId));

        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());

        response.setCartItems(cart.getCartItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setQuantity(cartItem.getQuantity());

        // Ánh xạ product
        ProductCartResponse productCartResponse = new ProductCartResponse();
        productCartResponse.setId(cartItem.getProduct().getId());
        productCartResponse.setProductName(cartItem.getProduct().getProductName());
        productCartResponse.setPrice(cartItem.getProduct().getPrice());
        productCartResponse.setThumbImage(cartItem.getProduct().getThumbImage());
        response.setProduct(productCartResponse);

        return response;
    }


}