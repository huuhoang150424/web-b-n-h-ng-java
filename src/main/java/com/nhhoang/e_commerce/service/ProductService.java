package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateProductRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateProductRequest;
import com.nhhoang.e_commerce.dto.response.CategoryResponse;
import com.nhhoang.e_commerce.dto.response.ProductAttributeResponse;
import com.nhhoang.e_commerce.dto.response.ProductDetailResponse;
import com.nhhoang.e_commerce.dto.response.ProductRecentResponse;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

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

    public List<ProductRecentResponse> getRecentProducts() {
        List<Product> products = productRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        return products.stream().map(this::mapToProductResponse).collect(Collectors.toList());
    }

    private ProductRecentResponse mapToProductResponse(Product product) {
        ProductRecentResponse response = new ProductRecentResponse();
        response.setId(product.getId());
        response.setSlug(product.getSlug());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setThumbImage(product.getThumbImage());
        response.setStock(product.getStock());
        response.setImageUrls(product.getImageUrls());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus().name());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map Category
        if (product.getCategory() != null) {
            CategoryResponse categoryResponse = new CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setCategoryName(product.getCategory().getCategoryName());
            response.setCategory(categoryResponse);
        }

        // Map ProductAttributes
        response.setProductAttributes(
                product.getProductAttributes().stream().map(attr -> {
                    ProductAttributeResponse attrResponse = new ProductAttributeResponse();
                    attrResponse.setId(attr.getId());
                    attrResponse.setAttributeName(attr.getAttribute().getAttributeName());
                    attrResponse.setAttributeValue(attr.getValue());
                    return attrResponse;
                }).collect(Collectors.toList())
        );

        return response;
    }
}