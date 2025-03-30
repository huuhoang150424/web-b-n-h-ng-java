package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.FavoriteProduct;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.ProductAttribute;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional
    public void removeFavoriteProduct(User user, FavoriteProductRequest request) {
        logger.info("Removing favorite product for user: {}, productId: {}", user.getId(), request.getProductId());
        Optional<FavoriteProduct> favoriteProductOpt = favoriteProductRepository.findByUserIdAndProductId(
                user.getId(), request.getProductId()
        );

        if (favoriteProductOpt.isPresent()) {
            favoriteProductRepository.delete(favoriteProductOpt.get());
            logger.info("Successfully removed favorite product for user: {}, productId: {}", user.getId(), request.getProductId());
        } else {
            logger.warn("Favorite product not found for user: {}, productId: {}", user.getId(), request.getProductId());
            throw new IllegalArgumentException("Sản phẩm yêu thích không tồn tại");
        }
    }

    @Transactional(readOnly = true)
    public List<FavoriteProductResponse> getAllFavoriteProducts(User user) {
        logger.info("Fetching all favorite products for user: {}", user.getId());
        List<FavoriteProduct> favoriteProducts = favoriteProductRepository.findByUserIds(user.getId());

        List<FavoriteProductResponse> responses = favoriteProducts.stream()
                .filter(fp -> fp.getProduct() != null)
                .map(this::mapToFavoriteProductResponse)
                .collect(Collectors.toList());

        int filteredCount = favoriteProducts.size() - responses.size();
        if (filteredCount > 0) {
            logger.warn("Đã lọc bỏ {} favorite product có product null", filteredCount);
        }

        return responses;
    }
    private FavoriteProductResponse mapToFavoriteProductResponse(FavoriteProduct favoriteProduct) {
        FavoriteProductResponse response = new FavoriteProductResponse();
        response.setId(favoriteProduct.getId());
        response.setUserId(favoriteProduct.getUser().getId());
        response.setProduct(mapToProductResponse(favoriteProduct.getProduct()));
        return response;
    }

    private ProductFavoriteResponse mapToProductResponse(Product product) {
        ProductFavoriteResponse response = new ProductFavoriteResponse();
        response.setId(product.getId());
        response.setSlug(product.getSlug());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice() != null ? product.getPrice().floatValue() : null);
        response.setThumbImage(product.getThumbImage());
        response.setStock(product.getStock());
        response.setImageUrls(product.getImageUrls());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            CategoryResponse categoryResponse = new CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setCategoryName(product.getCategory().getCategoryName());
            response.setCategory(categoryResponse);
        }

        if (product.getProductAttributes() != null) {
            List<ProductAttributeResponse> attributes = new ArrayList<>();
            for (ProductAttribute attr : product.getProductAttributes()) {
                ProductAttributeResponse attrResponse = new ProductAttributeResponse();
                attrResponse.setId(attr.getId());
                attrResponse.setAttributeName(attr.getAttribute() != null ? attr.getAttribute().getAttributeName() : null);
                attrResponse.setAttributeValue(attr.getValue());
                attributes.add(attrResponse);
            }
            response.setProductAttributes(attributes);
        }

        return response;
    }
}