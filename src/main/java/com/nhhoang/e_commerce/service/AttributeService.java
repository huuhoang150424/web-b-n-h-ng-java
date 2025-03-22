package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateAttributeRequest;
import com.nhhoang.e_commerce.entity.Attributes;
import com.nhhoang.e_commerce.repository.AttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttributeService {

    @Autowired
    private AttributeRepository attributeRepository;

    public void createAttribute(CreateAttributeRequest request) {
        if (attributeRepository.existsByAttributeName(request.getAttributeName())) {
            throw new IllegalArgumentException("Thuộc tính này đã tồn tại");
        }

        Attributes newAttribute = new Attributes();
        newAttribute.setAttributeName(request.getAttributeName());

        attributeRepository.save(newAttribute);
    }
}