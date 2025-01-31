package com.example.samuraitravel.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.form.ReviewForm;
import com.example.samuraitravel.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	
	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	
	public Page<Review> getReviewsByHouse(House house, Pageable pageable) {
        return reviewRepository.findByHouseOrderByCreatedAtDesc(house, pageable);
    }

    public void addReview(Review review) {
        reviewRepository.save(review);
    }
    
    public Optional<Review> findById(Integer reviewId) {
        return reviewRepository.findById(reviewId);
    }
    
    public Review findByReservation(Reservation reservation) {
        return reviewRepository.findByReservation(reservation)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for Reservation ID: " + reservation));
    }

    public void editReview(Review review) {
        reviewRepository.save(review); // 更新
    }

    public void deleteById(Integer id) {
        reviewRepository.deleteById(id);
    }
    
 // **レビューの更新処理**
    @Transactional
    public void updateReview(Integer id, ReviewForm reviewForm) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for ID: " + id));

        // レビュー情報を更新
        review.setRating(reviewForm.getRating());
        review.setComment(reviewForm.getComment());
        review.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now())); // 更新日時を追加する場合

        // データベースへ保存
        reviewRepository.save(review);
    }
    
    public void save(Review review) {
        reviewRepository.save(review);
    }
}
