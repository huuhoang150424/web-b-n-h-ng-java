package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateCategoryRequest;
import com.nhhoang.e_commerce.entity.Category;
import com.nhhoang.e_commerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}