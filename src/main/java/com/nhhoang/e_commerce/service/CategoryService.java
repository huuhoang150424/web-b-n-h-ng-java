package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateCategoryRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateCategoryRequest;
import com.nhhoang.e_commerce.dto.response.CategoryResponse;
import com.nhhoang.e_commerce.entity.Category;
import com.nhhoang.e_commerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

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

    public void deleteCategory(String catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

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
}