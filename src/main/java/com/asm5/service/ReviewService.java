package com.asm5.service;

import java.util.List;

import com.asm5.model.Review;

public interface ReviewService {
    List<Review> getReviewsByProduct(Integer productId);
    Review saveReview(Review review);
    void deleteReview(Integer id);
    List<Review> getAllReviewsByProduct(Integer productId);

}
