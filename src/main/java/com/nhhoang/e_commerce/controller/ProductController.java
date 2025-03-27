package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.CreateProductRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateProductRequest;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.ProductService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;

    @PostMapping("/createProduct")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request,
                                           BindingResult bindingResult,
                                           HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse(null, errors));
            }

            productService.createProduct(request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Tạo sản phẩm mới thành công");

            return ResponseEntity.status(201).body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllProduct")
    public ResponseEntity<?> getAllProducts(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            Map<String, Object> result = new HashMap<>();
            List<ProductResponse> productData;

            if (page == null && size == null) {
                productData = productService.getAllProducts().stream()
                        .map(this::mapToProductResponse)
                        .collect(Collectors.toList());

                result.put("totalItems", productData.size());
                result.put("currentPage", 1);
                result.put("totalPages", 1);
                result.put("pageSize", productData.size());
                result.put("data", productData);
            } else {
                // Có phân trang
                int pageNum = page != null ? page - 1 : 0;
                int pageSize = size != null ? size : 10;
                if (pageSize > 100) pageSize = 100;

                Page<Product> productPage = productService.getAllProductsPaginated(pageNum, pageSize);
                productData = productPage.getContent().stream()
                        .map(this::mapToProductResponse)
                        .collect(Collectors.toList());

                result.put("totalItems", productPage.getTotalElements());
                result.put("currentPage", productPage.getNumber() + 1);
                result.put("totalPages", productPage.getTotalPages());
                result.put("pageSize", productPage.getSize());
                result.put("data", productData);
            }

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSlug(product.getSlug());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setThumbImage(product.getThumbImage());
        response.setStock(product.getStock());
        response.setImageUrls(product.getImageUrls());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCategoryId(product.getCategory().getId());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    @GetMapping("/getProduct/{slug}")
    public ResponseEntity<?> getProduct(@PathVariable String slug, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            ProductDetailResponse product = productService.getProductBySlug(slug);

            Map<String, Object> result = new HashMap<>();
            result.put("data", product);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Sản phẩm không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @PutMapping("/editProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id,
                                           @Valid @RequestBody UpdateProductRequest request,
                                           BindingResult bindingResult,
                                           HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Dữ liệu không hợp lệ", errors));
            }

            productService.updateProduct(id, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cập nhật sản phẩm thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            productService.deleteProduct(id);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Sản phẩm đã được xóa thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy sản phẩm"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getProductRecent")
    public ResponseEntity<?> getProductRecent(HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            List<ProductRecentResponse> recentProducts = productService.getRecentProducts();
            Map<String, Object> result = new HashMap<>();
            result.put("data", recentProducts);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @GetMapping("/getProductClient/{id}")
    @Cacheable(value = "getProductClient", unless = "#result == null or #result.body == null")
    public ResponseEntity<?> getProductClient(HttpServletRequest httpRequest, @PathVariable String id) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                logger.warn("Current user is null for request to getProductClient with id: {}", id);
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            ProductClientResponse product = productService.getProductClient(id, currentUser);
            Map<String, Object> result = new HashMap<>();
            result.put("data", product);
            logger.info("Successfully processed getProductClient for id: {}", id);
            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found for id: {}", id, e);
            return ResponseEntity.status(404).body(new ErrorResponse("Sản phẩm không tồn tại"));
        } catch (Exception e) {
            logger.error("Error processing getProductClient for id: {}", id, e);
            return ResponseEntity.status(500).body(new ErrorResponse("Internal Server Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    @GetMapping("/similar")
    public ResponseEntity<?> getSimilarProducts(
            HttpServletRequest request,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for similar products request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<String> similarProducts = productService.findSimilarProducts(keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("data", similarProducts);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching similar products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            HttpServletRequest request,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for search request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            ProductService.SearchResult searchResult = productService.findProductsByKeyword(keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("data", searchResult.getData());
            result.put("keyword", searchResult.getKeyword());

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/{categoryId}/category")
    public ResponseEntity<?> getProductByCat(HttpServletRequest request, @PathVariable String categoryId) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get products by category request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<ProductByCategoryResponse> products = productService.getProductsByCategory(categoryId);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", products);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching products by category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/getProductByStar/{countStar}")
    public ResponseEntity<?> getProductByStar(HttpServletRequest request, @PathVariable Integer countStar
                                              ) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get products by star request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            if (countStar == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("countStar là bắt buộc"));
            }

            List<GetProductByStartResponse> products = productService.getProductsByStar(countStar);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", products);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching products by star: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }

    }

    @GetMapping("/getProductByPrice")
    public ResponseEntity<?> getProductByPrice(HttpServletRequest request,
                                               @RequestParam(value = "minPrice", required = false) String minPrice,
                                               @RequestParam(value = "maxPrice", required = false) String maxPrice) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get products by price request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (minPrice == null || maxPrice == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Vui lòng cung cấp minPrice và maxPrice"));
            }
            Double minPriceValue;
            Double maxPriceValue;
            try {
                minPriceValue = Double.parseDouble(minPrice);
                maxPriceValue = Double.parseDouble(maxPrice);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("minPrice và maxPrice phải là số hợp lệ"));
            }

            List<GetProductByStartResponse> products = productService.getProductsByPrice(minPriceValue, maxPriceValue);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Thành công");
            result.put("data", products);
            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching products by price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}