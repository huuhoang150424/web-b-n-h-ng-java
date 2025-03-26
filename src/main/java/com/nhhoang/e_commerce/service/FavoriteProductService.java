package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.entity.FavoriteProduct;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.FavoriteProductRepository;
import com.nhhoang.e_commerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteProductService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteProductService.class);

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public FavoriteProduct addFavoriteProduct(User user, FavoriteProductRequest request) {
        logger.info("Adding favorite product for user: {}, productId: {}", user.getId(), request.getProductId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product not found with id: {}", request.getProductId());
                    return new IllegalArgumentException("Product not found");
                });

        if (favoriteProductRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            logger.warn("Product {} is already favorited by user {}", product.getId(), user.getId());
            throw new IllegalStateException("Sản phẩm này đã được yêu thích bởi người dùng này.");
        }
        FavoriteProduct favoriteProduct = new FavoriteProduct();
        favoriteProduct.setUser(user);
        favoriteProduct.setProduct(product);

        FavoriteProduct saved = favoriteProductRepository.save(favoriteProduct);
        logger.info("Successfully added favorite product with id: {}", saved.getId());
        return saved;
    }
}