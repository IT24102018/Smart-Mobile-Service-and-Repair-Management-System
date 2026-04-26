package com.mobix.service;

import com.mobix.model.Review;
import com.mobix.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findByOrderByReviewDateDesc();
    }

    public void deleteReviewById(Long id) {
        if (id != null && reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
        }
    }
}
