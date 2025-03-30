package com.nhhoang.e_commerce.service;

import com.nhhoang.e_commerce.dto.requests.*;
import com.nhhoang.e_commerce.dto.response.*;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.repository.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Transactional
    public String rateProduct(User user, String productId, RatingRequest ratingRequest) {
        logger.info("User {} is rating product {}", user.getId(), productId);

        // Tìm sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));

        // Tìm rating hiện có
        Optional<Rating> existingRating = ratingRepository.findByUserAndProduct(user, product);

        if (existingRating.isPresent()) {
            // Cập nhật rating
            Rating rating = existingRating.get();
            rating.setRating(ratingRequest.getRating());
            ratingRepository.save(rating);
            return "Đánh giá đã được cập nhật thành công.";
        } else {
            // Tạo rating mới
            Rating newRating = new Rating();
            newRating.setUser(user);
            newRating.setProduct(product);
            newRating.setRating(ratingRequest.getRating());
            ratingRepository.save(newRating);
            return "Đánh giá thành công.";
        }
    }


    @Transactional(readOnly = true)
    public ReviewResult getProductReviews(Integer page, Integer size) {
        logger.info("Fetching product reviews with page: {}, size: {}", page, size);

        List<ReviewResponse> reviews;
        long totalItems;

        if (page == null && size == null) {
            List<Comment> comments = commentRepository.findAll();
            reviews = processComments(comments);
            totalItems = reviews.size();
            return new ReviewResult(totalItems, 1, 1, reviews.size(), reviews);
        }
        int pageNumber = page != null && page > 0 ? page - 1 : 0;
        int pageSize = size != null && size > 0 && size <= MAX_PAGE_SIZE ? size : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Comment> commentPage = commentRepository.findAll(pageable);
        reviews = processComments(commentPage.getContent());
        totalItems = commentPage.getTotalElements();

        return new ReviewResult(
                totalItems,
                commentPage.getNumber() + 1,
                commentPage.getTotalPages(),
                commentPage.getSize(),
                reviews
        );
    }

    private List<ReviewResponse> processComments(List<Comment> comments) {
        // Nhóm các comment theo user_id và product_id, xử lý trường hợp product null
        Map<String, List<Comment>> groupedComments = comments.stream()
                .collect(Collectors.groupingBy(
                        comment -> {
                            String productId = (comment.getProduct() != null) ? comment.getProduct().getId() : "null";
                            return comment.getUser().getId() + "_" + productId;
                        }
                ));

        List<ReviewResponse> reviews = new ArrayList<>();

        for (List<Comment> commentList : groupedComments.values()) {
            Comment firstComment = commentList.get(0);
            ReviewResponse responseWithRating = mapToReviewResponse(firstComment, true);
            reviews.add(responseWithRating);
            for (int i = 1; i < commentList.size(); i++) {
                ReviewResponse responseWithoutRating = mapToReviewResponse(commentList.get(i), false);
                reviews.add(responseWithoutRating);
            }
        }

        return reviews;
    }

    private ReviewResponse mapToReviewResponse(Comment comment, boolean includeRating) {
        ReviewResponse response = new ReviewResponse();

        // Xử lý product (nếu null thì để null)
        if (comment.getProduct() != null) {
            ReviewResponse.ProductReviewResponse product = new ReviewResponse.ProductReviewResponse();
            product.setProductName(comment.getProduct().getProductName());
            product.setThumbImage(comment.getProduct().getThumbImage());
            response.setProduct(product);
        } else {
            response.setProduct(null); // Nếu sản phẩm là null, không trả về thông tin sản phẩm
        }

        // Xử lý user
        ReviewResponse.UserReviewResponse user = new ReviewResponse.UserReviewResponse();
        user.setAvatar(comment.getUser().getAvatar());
        user.setName(comment.getUser().getName());
        response.setUser(user);

        // Xử lý comment
        response.setComment(comment.getComment());

        // Xử lý rating
        if (includeRating && comment.getProduct() != null) { // Chỉ lấy rating nếu product không null
            Optional<Rating> rating = ratingRepository.findByUserAndProduct(comment.getUser(), comment.getProduct());
            response.setRating(rating.isPresent() ? rating.get().getRating() : null);
        } else {
            response.setRating(null);
        }

        return response;
    }

    @Data
    public static class ReviewResult {
        private final long totalItems;
        private final int currentPage;
        private final int totalPages;
        private final int pageSize;
        private final List<ReviewResponse> data;

        public ReviewResult(long totalItems, int currentPage, int totalPages, int pageSize, List<ReviewResponse> data) {
            this.totalItems = totalItems;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.pageSize = pageSize;
            this.data = data;
        }
    }
}