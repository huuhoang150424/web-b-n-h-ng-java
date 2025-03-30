package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.CategoryRepository;
import com.nhhoang.e_commerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public void createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new IllegalArgumentException("Danh mục đã tồn tại");
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(request.getCategoryName());
        newCategory.setImage(request.getImage());

        categoryRepository.save(newCategory);
    }
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by("id"));
    }

    public Page<Category> getAllCategoriesPaginated(int page, int size) {
        return categoryRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
    }

    @Transactional
    public void deleteCategory(String catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        // Tìm tất cả Product liên quan và đặt category thành null
        List<Product> products = productRepository.findByCategoryId(catId);
        for (Product product : products) {
            product.setCategory(null);
            productRepository.save(product);
        }

        // Xóa Category
        categoryRepository.delete(category);
    }
    public CategoryResponse getCategory(String catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setImage(category.getImage());

        return response;
    }

    public void updateCategory(String catId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        if (request.getCategoryName() != null) {
            category.setCategoryName(request.getCategoryName());
        }
        if (request.getImage() != null) {
            category.setImage(request.getImage());
        }

        categoryRepository.save(category);
    }

    public List<CategoryClientResponse> getTopCategoriesByProductCount() {
        List<Category> categories = categoryRepository.findTop5ByOrderByProductCountDesc();
        return categories.stream()
                .map(this::mapToCategoryClientResponse)
                .limit(5)
                .collect(Collectors.toList());
    }

    private CategoryClientResponse mapToCategoryClientResponse(Category category) {
        CategoryClientResponse response = new CategoryClientResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return response;
    }
}