package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.CreateProductRequest;
import com.nhhoang.e_commerce.dto.response.ProductDetailResponse;
import com.nhhoang.e_commerce.dto.response.ProductResponse;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.ProductService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {

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
}