package com.nhhoang.e_commerce.elasticsearch.repository;

import com.nhhoang.e_commerce.elasticsearch.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    List<ProductDocument> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String desc);
}
