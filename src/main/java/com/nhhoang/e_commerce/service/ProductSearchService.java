package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.elasticsearch.document.ProductDocument;
import com.nhhoang.e_commerce.elasticsearch.repository.ProductElasticsearchRepository;
import com.nhhoang.e_commerce.entity.Product;
import com.nhhoang.e_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    @Autowired(required = false)
    private ProductElasticsearchRepository elasticsearchRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDocument> searchProducts(String keyword) {
        try {
            if (elasticsearchRepository != null && elasticsearchRepository.count() > 0) {
                System.out.println("🔍 [Elasticsearch Engine] Searching for keyword: " + keyword);
                return elasticsearchRepository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
            }
        } catch (Exception e) {
            System.err.println("⚠️ [Elasticsearch Fallback] ES Cluster unavailable, falling back to MySQL JPA search: " + e.getMessage());
        }

        // Fallback to MySQL JPA search
        System.out.println("🔍 [MySQL JPA Engine Fallback] Searching for keyword: " + keyword);
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);
        return products.stream()
                .map(p -> ProductDocument.builder()
                        .id(p.getId())
                        .productName(p.getProductName())
                        .slug(p.getSlug())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : "N/A")
                        .thumbImage(p.getThumbImage())
                        .build())
                .collect(Collectors.toList());
    }

    public void indexProduct(Product product) {
        try {
            if (elasticsearchRepository != null) {
                ProductDocument doc = ProductDocument.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .slug(product.getSlug())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : "N/A")
                        .thumbImage(product.getThumbImage())
                        .build();
                elasticsearchRepository.save(doc);
                System.out.println("✅ [Elasticsearch Indexer] Indexed product: " + product.getProductName());
            }
        } catch (Exception e) {
            System.err.println("⚠️ [Elasticsearch Index Warning] " + e.getMessage());
        }
    }
}
