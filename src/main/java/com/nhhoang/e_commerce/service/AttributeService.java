package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateAttributeRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateAttributeRequest;
import com.nhhoang.e_commerce.entity.Attributes;
import com.nhhoang.e_commerce.entity.ProductAttribute;
import com.nhhoang.e_commerce.repository.AttributeRepository;
import com.nhhoang.e_commerce.repository.ProductAttributesRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeService {

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private ProductAttributesRepository productAttributeRepository;

    public void createAttribute(CreateAttributeRequest request) {
        if (attributeRepository.existsByAttributeName(request.getAttributeName())) {
            throw new IllegalArgumentException("Thuộc tính này đã tồn tại");
        }

        Attributes newAttribute = new Attributes();
        newAttribute.setAttributeName(request.getAttributeName());

        attributeRepository.save(newAttribute);
    }

    public void updateAttribute(String id, UpdateAttributeRequest request) {
        Attributes attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thuộc tính này không tồn tại"));

        attribute.setAttributeName(request.getAttributeName());

        attributeRepository.save(attribute);
    }

    public List<Attributes> getAllAttributes() {
        return attributeRepository.findAll(Sort.by("id"));
    }

    public Page<Attributes> getAllAttributesPaginated(int page, int size) {
        return attributeRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
    }

    @Transactional
    public void deleteAttribute(String id) {
        if (!attributeRepository.existsById(id)) {
            throw new IllegalArgumentException("Thuộc tính không tồn tại");
        }

        // Tìm tất cả ProductAttribute liên quan và đặt attribute thành null
        List<ProductAttribute> productAttributes = productAttributeRepository.findByAttributeId(id);
        for (ProductAttribute productAttribute : productAttributes) {
            productAttribute.setAttribute(null);
            productAttributeRepository.save(productAttribute);
        }

        // Xóa Attribute
        attributeRepository.deleteById(id);
    }
}