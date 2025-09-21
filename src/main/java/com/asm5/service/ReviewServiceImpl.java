package com.asm5.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asm5.model.Review;
import com.asm5.repository.ReviewRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Override
    public List<Review> getReviewsByProduct(Integer productId) {
        // Lấy 3 review mới nhất
        return reviewRepository.findTop3ByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public List<Review> getAllReviewsByProduct(Integer productId) {
        // Lấy tất cả review
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }
}

