package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.service.CategoryService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/category")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/createCat")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request,
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
                return ResponseEntity.badRequest().body(new ErrorResponse("Tạo thư mục mới thất bại", errors));
            }

            categoryService.createCategory(request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Tạo danh mục mới thành công");

            return ResponseEntity.status(201).body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Danh mục đã tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllCat")
    public ResponseEntity<?> getAllCategories(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size,
                                              HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            Map<String, Object> result = new HashMap<>();
            List<CategoryResponse> categoryData;

            if (page == null && size == null) {
                List<Category> allCategories = categoryService.getAllCategories();
                categoryData = allCategories.stream().map(this::mapToCategoryResponse).collect(Collectors.toList());

                result.put("totalItems", categoryData.size());
                result.put("currentPage", 1);
                result.put("totalPages", 1);
                result.put("pageSize", categoryData.size());
                result.put("data", categoryData);
            } else {
                int pageNum = page != null ? page - 1 : 0; // Spring dùng 0-based index
                int pageSize = size != null ? size : 10;
                if (pageSize > 100) pageSize = 100;

                Page<Category> categoryPage = categoryService.getAllCategoriesPaginated(pageNum, pageSize);
                categoryData = categoryPage.getContent().stream().map(this::mapToCategoryResponse).collect(Collectors.toList());

                result.put("totalItems", categoryPage.getTotalElements());
                result.put("currentPage", categoryPage.getNumber() + 1); // Chuyển về 1-based
                result.put("totalPages", categoryPage.getTotalPages());
                result.put("pageSize", categoryPage.getSize());
                result.put("data", categoryData);
            }

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setImage(category.getImage());
        return response;
    }

    @DeleteMapping("/deleteCat/{catId}")
    public ResponseEntity<?> deleteCategory(@PathVariable String catId, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            categoryService.deleteCategory(catId);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Danh mục đã được xóa thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Danh mục không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getCat/{catId}")
    public ResponseEntity<?> getCategory(@PathVariable String catId, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            CategoryResponse category = categoryService.getCategory(catId);

            Map<String, Object> result = new HashMap<>();
            result.put("data", category);

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Danh mục không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi không xác định"));
        }
    }

    @PutMapping("/updateCat/{catId}")
    public ResponseEntity<?> updateCategory(@PathVariable String catId,
                                            @RequestBody UpdateCategoryRequest request,
                                            HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            categoryService.updateCategory(catId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cập nhật danh mục thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Danh mục không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Đã xảy ra lỗi trong quá trình xử lý: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllClient")
    public ResponseEntity<?> getAllClient(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("user");
            if (currentUser == null) {
                logger.warn("User not authenticated for get all client categories request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn cần đăng nhập"));
            }

            List<CategoryClientResponse> categories = categoryService.getTopCategoriesByProductCount();

            Map<String, Object> result = new HashMap<>();
            result.put("data", categories);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            logger.error("Error fetching top categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi không xác định"));
        }
    }
}