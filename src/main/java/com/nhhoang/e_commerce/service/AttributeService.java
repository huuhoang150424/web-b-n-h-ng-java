package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.CreateAttributeRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateAttributeRequest;
import com.nhhoang.e_commerce.entity.Attributes;
import com.nhhoang.e_commerce.repository.AttributeRepository;
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

    public void deleteAttribute(String id) {
        if (!attributeRepository.existsById(id)) {
            throw new IllegalArgumentException("Thuộc tính không tồn tại");
        }

        attributeRepository.deleteById(id);
    }
}