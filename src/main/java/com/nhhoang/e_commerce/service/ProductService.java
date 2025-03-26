package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateProductRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateProductRequest;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private ProductAttributesRepository productAttributesRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public void createProduct(CreateProductRequest request) {
        if (productRepository.existsByProductName(request.getProductName())) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setThumbImage(request.getThumbImage());
        product.setStock(request.getStock());
        product.setImageUrls(request.getImageUrls());
        product.setCategory(category);
        product.setDescription(request.getDescription());

        productRepository.save(product);

        if (request.getAttributes() != null) {
            for (CreateProductRequest.AttributeData attrData : request.getAttributes()) {
                Attributes attribute = attributeRepository.findByAttributeName(attrData.getAttributeName())
                        .orElseThrow(() -> new IllegalArgumentException("Thuộc tính \"" + attrData.getAttributeName() + "\" không tồn tại."));

                ProductAttribute productAttribute = new ProductAttribute();
                productAttribute.setProduct(product);
                productAttribute.setAttribute(attribute);
                productAttribute.setValue(attrData.getValue());

                productAttributesRepository.save(productAttribute);
            }
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll(Sort.by("id"));
    }

    public Page<Product> getAllProductsPaginated(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
    }

    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setSlug(product.getSlug());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setThumbImage(product.getThumbImage());
        response.setStock(product.getStock());
        response.setImageUrls(product.getImageUrls());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Category
        ProductDetailResponse.CategoryResponse categoryResponse = new ProductDetailResponse.CategoryResponse();
        categoryResponse.setId(product.getCategory().getId());
        categoryResponse.setCategoryName(product.getCategory().getCategoryName());
        categoryResponse.setImage(product.getCategory().getImage());
        response.setCategory(categoryResponse);

        // Product Attributes
        response.setProductAttributes(product.getProductAttributes().stream()
                .map(pa -> {
                    ProductDetailResponse.ProductAttributeResponse attrResponse = new ProductDetailResponse.ProductAttributeResponse();
                    attrResponse.setId(pa.getId());
                    attrResponse.setAttributeName(pa.getAttribute().getAttributeName());
                    attrResponse.setValue(pa.getValue());
                    return attrResponse;
                })
                .collect(Collectors.toList()));

        return response;
    }

    public void updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setThumbImage(request.getThumbImage());
        product.setStock(request.getStock());
        product.setImageUrls(request.getImageUrls());
        product.setCategory(category);
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());

        productRepository.save(product);

        productAttributesRepository.deleteAll(product.getProductAttributes());

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (UpdateProductRequest.AttributeData attrData : request.getAttributes()) {
                Attributes attribute = attributeRepository.findByAttributeName(attrData.getAttributeName())
                        .orElseThrow(() -> new IllegalArgumentException("Thuộc tính \"" + attrData.getAttributeName() + "\" không tồn tại"));

                ProductAttribute productAttribute = new ProductAttribute();
                productAttribute.setId(UUID.randomUUID().toString());
                productAttribute.setProduct(product);
                productAttribute.setAttribute(attribute);
                productAttribute.setValue(attrData.getValue());

                productAttributesRepository.save(productAttribute);
            }
        }
    }

    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm");
        }
        List<Comment> comments = commentRepository.findByProductId(id);
        for (Comment comment : comments) {
            comment.setProduct(null);
            commentRepository.save(comment);
        }
        List<Rating> ratings = ratingRepository.findByProductId(id);
        for (Rating rating : ratings) {
            rating.setProduct(null);
            ratingRepository.save(rating);
        }
        List<ProductAttribute> productAttributes = productAttributesRepository.findByProductId(id);
        for (ProductAttribute productAttribute : productAttributes) {
            productAttribute.setProduct(null);
            productAttributesRepository.save(productAttribute);
        }
        List<FavoriteProduct> favoriteProducts = favoriteProductRepository.findByProductId(id);
        for (FavoriteProduct favoriteProduct : favoriteProducts) {
            favoriteProduct.setProduct(null);
            favoriteProductRepository.save(favoriteProduct);
        }
        List<CartItem> cartItems = cartItemRepository.findByProductId(id);
        for (CartItem cartItem : cartItems) {
            cartItem.setProduct(null);
            cartItemRepository.save(cartItem);
        }
        List<OrderDetail> orderDetails = orderDetailRepository.findByProductId(id);
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setProduct(null);
            orderDetailRepository.save(orderDetail);
        }
        productRepository.deleteById(id);
    }

    @Cacheable(value = "recentProducts", unless = "#result == null or #result.isEmpty()")
    public List<ProductRecentResponse> getRecentProducts() {
        logger.info("Fetching recent products");
        List<Product> products = productRepository.findRecentProducts(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<ProductRecentResponse> result = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
        logger.info("Fetched {} recent products", result.size());
        return result;
    }

    private ProductRecentResponse mapToProductResponse(Product product) {
        logger.info("Mapping product: {}", product.getId());
        ProductRecentResponse response = new ProductRecentResponse();
        try {
            response.setId(product.getId());
            response.setSlug(product.getSlug());
            response.setProductName(product.getProductName());
            response.setPrice(product.getPrice());
            response.setThumbImage(product.getThumbImage());
            response.setStock(product.getStock());
            response.setImageUrls(product.getImageUrls());
            response.setDescription(product.getDescription());
            response.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
            response.setCreatedAt(product.getCreatedAt());
            response.setUpdatedAt(product.getUpdatedAt());

            // Map Category
            if (product.getCategory() != null) {
                CategoryResponse categoryResponse = new CategoryResponse();
                categoryResponse.setId(product.getCategory().getId());
                categoryResponse.setCategoryName(product.getCategory().getCategoryName());
                response.setCategory(categoryResponse);
                logger.info("Set category: id={}, name={}", categoryResponse.getId(), categoryResponse.getCategoryName());
            } else {
                logger.warn("Category is null for product: {}", product.getId());
            }

            // Map ProductAttributes
            if (product.getProductAttributes() != null) {
                response.setProductAttributes(
                        product.getProductAttributes().stream().map(attr -> {
                            ProductAttributeResponse attrResponse = new ProductAttributeResponse();
                            attrResponse.setId(attr.getId());
                            if (attr.getAttribute() != null) {
                                attrResponse.setAttributeName(attr.getAttribute().getAttributeName());
                            } else {
                                logger.warn("Attribute is null for product attribute id: {}", attr.getId());
                                attrResponse.setAttributeName(null);
                            }
                            attrResponse.setAttributeValue(attr.getValue());
                            return attrResponse;
                        }).collect(Collectors.toList())
                );
                logger.info("Set productAttributes: size={}", response.getProductAttributes().size());
            } else {
                logger.warn("ProductAttributes is null for product: {}", product.getId());
                response.setProductAttributes(Collections.emptyList());
            }

            logger.info("Successfully mapped product: {}", product.getId());
            return response;
        } catch (Exception e) {
            logger.error("Error mapping product {}: {}", product.getId(), e.getMessage(), e);
            throw e;
        }
    }
    public ProductClientResponse getProductClient(String id, User user) {
        logger.info("Fetching product with id: {}", id);
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        logger.info("Product fetched: id={}, name={}", product.getId(), product.getProductName());
        return mapToProductResponse(product, user);
    }

    private ProductClientResponse mapToProductResponse(Product product, User user) {
        logger.info("Starting mapping product: {}", product.getId());
        ProductClientResponse response = new ProductClientResponse();

        try {
            response.setId(product.getId());
            response.setSlug(product.getSlug());
            response.setProductName(product.getProductName());
            response.setPrice(product.getPrice());
            response.setThumbImage(product.getThumbImage());
            response.setStock(product.getStock());
            response.setImageUrls(product.getImageUrls());
            response.setDescription(product.getDescription());
            response.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
            response.setCreatedAt(product.getCreatedAt());
            response.setUpdatedAt(product.getUpdatedAt());

            // Category
            if (product.getCategory() != null) {
                CategoryResponse category = new CategoryResponse();
                category.setId(product.getCategory().getId());
                category.setCategoryName(product.getCategory().getCategoryName());
                category.setImage(product.getCategory().getImage());
                response.setCategory(category);
            } else {
                logger.warn("Category is null for product: {}", product.getId());
            }

            // Product Attributes
            if (product.getProductAttributes() != null) {
                response.setProductAttributes(
                        product.getProductAttributes().stream().map(attr -> {
                            ProductAttributeResponse attrResponse = new ProductAttributeResponse();
                            attrResponse.setId(attr.getId());
                            if (attr.getAttribute() != null) {
                                attrResponse.setAttributeName(attr.getAttribute().getAttributeName());
                            } else {
                                attrResponse.setAttributeName(null);
                            }
                            attrResponse.setAttributeValue(attr.getValue());
                            return attrResponse;
                        }).collect(Collectors.toList())
                );
            } else {
                logger.warn("Product attributes are null for product: {}", product.getId());
                response.setProductAttributes(Collections.emptyList());
            }

            // Ratings
            if (product.getRatings() != null) {
                response.setRatings(
                        product.getRatings().stream().map(rating -> {
                            RatingResponse ratingResponse = new RatingResponse();
                            ratingResponse.setRating(rating.getRating());
                            ratingResponse.setCreatedAt(rating.getCreatedAt());
                            if (rating.getUser() != null) {
                                ratingResponse.setUserInfo(new UserInfoResponse(rating.getUser().getId()));
                            } else {
                                ratingResponse.setUserInfo(null);
                            }
                            return ratingResponse;
                        }).collect(Collectors.toList())
                );

                if (!product.getRatings().isEmpty()) {
                    double averageRating = product.getRatings().stream()
                            .mapToDouble(r -> r.getRating())
                            .average()
                            .orElse(0.0);
                    response.setAverageRating(averageRating);
                } else {
                    response.setAverageRating(null);
                }
            } else {
                response.setRatings(Collections.emptyList());
                response.setAverageRating(null);
            }

            // Is Favorite
            if (user != null && product != null) {
                boolean isFavorite = favoriteProductRepository.existsByUserAndProduct(user, product);
                response.setIsFavorite(isFavorite);
            } else {
                response.setIsFavorite(false);
            }

            return response;

        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<String> findSimilarProducts(String keyword) {
        logger.info("Searching for products similar to keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword parameter is required");
        }

        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword.trim());
        return products.stream()
                .map(Product::getProductName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SearchResult findProductsByKeyword(String keyword) {
        logger.info("Searching for products with keyword: {}", keyword);

        String searchKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(searchKeyword);

        List<ProductSearchResponse> productResponses = products.stream()
                .map(this::mapToProductSearchResponse)
                .collect(Collectors.toList());

        List<String> listKeyword = products.stream()
                .map(Product::getProductName)
                .collect(Collectors.toList());

        return new SearchResult(productResponses, listKeyword);
    }

    private ProductSearchResponse mapToProductSearchResponse(Product product) {
        ProductSearchResponse response = new ProductSearchResponse();
        response.setId(product.getId());
        response.setSlug(product.getSlug());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setThumbImage(product.getThumbImage());
        response.setStock(product.getStock());
        response.setImageUrls(product.getImageUrls());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    public static class SearchResult {
        private final List<ProductSearchResponse> data;
        private final List<String> keyword;
        public SearchResult(List<ProductSearchResponse> data, List<String> keyword) {
            this.data = data;
            this.keyword = keyword;
        }
        public List<ProductSearchResponse> getData() {
            return data;
        }
        public List<String> getKeyword() {
            return keyword;
        }
    }
}