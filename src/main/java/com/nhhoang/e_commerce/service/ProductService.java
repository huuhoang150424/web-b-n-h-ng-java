package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateProductRequest;
import com.nhhoang.e_commerce.entity.Attributes;
import com.nhhoang.e_commerce.entity.Category;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.entity.ProductAttribute;
import com.nhhoang.e_commerce.repository.AttributeRepository;
import com.nhhoang.e_commerce.repository.CategoryRepository;
import com.nhhoang.e_commerce.repository.ProductAttributesRepository;
import com.nhhoang.e_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}