package com.example.samuraitravel.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReservationRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.repository.UserRepository;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    
    public ReviewController(ReviewService reviewService, HouseRepository houseRepository, 
    						UserRepository userRepository,ReviewRepository reviewRepository,
    						ReservationRepository reservationRepository) {
        this.reviewService = reviewService;
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }
    
    @GetMapping("/houses/{id}/reviews")
    public String showReviews(@PathVariable Integer id, Model model, 
                              @PageableDefault(size = 10) Pageable pageable) {
        House house = houseRepository.findById(id)
                     .orElseThrow(() -> new IllegalArgumentException("Invalid house ID: " + id));

        Page<Review> reviews = reviewRepository.findByHouse(house, pageable);

        model.addAttribute("house", house);
        model.addAttribute("reviews", reviews.getContent()); // ここで reviews をページング
        model.addAttribute("page", reviews);
        return "houses/show";
    }

    @GetMapping("/list")
    public String listReviews(@PathVariable Integer houseId, Model model, @PageableDefault(size = 10) Pageable pageable) {  
    	House house = houseRepository.getReferenceById(houseId);
        model.addAttribute("house", house);
        model.addAttribute("reviews", reviewService.getReviewsByHouse(house, pageable));
        return "reviews/list";
    }
    
    @GetMapping("/input")
    public String showReviewInputForm(@RequestParam Integer houseId, @RequestParam Integer reservationId, Model model) {
        // houseId に基づいてデータを取得
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid house ID: " + houseId));
        
        // 空のフォームオブジェクトを作成
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setHouseId(house.getId());
        reviewForm.setReservationId(reservationId);

        // モデルにデータを追加
        model.addAttribute("house", house);
        model.addAttribute("reviewForm", reviewForm);

        return "reviews/input"; // フォーム用ビューを返す
    }

    @PostMapping("/submit")
    public String submitReview(@ModelAttribute ReviewForm reviewForm, Authentication authentication) {
        // 認証されたユーザー情報を取得
    	User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + authentication.getName());
        }
    	// houseId を使って House エンティティを取得
        House house = houseRepository.findById(reviewForm.getHouseId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid house ID: " + reviewForm.getHouseId()));
        
     // reservationId を使って Reservation エンティティを取得（予約情報が必要）
        Reservation reservation = reservationRepository.findById(reviewForm.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + reviewForm.getReservationId()));

        // Review エンティティを作成
        Review review = new Review(); // review オブジェクトを定義
        review.setHouse(house);
        review.setUser(user);
        review.setReservation(reservation);
        review.setRating(reviewForm.getRating());
        review.setComment(reviewForm.getComment());
        review.setCreatedAt(Timestamp.valueOf(LocalDateTime.now())); // LocalDateTime を Timestamp に変換

        // レビューを保存
        reviewRepository.save(review);

        return "redirect:/reservations"; // 登録後に予約一覧ページへリダイレクト
    }
    
    @GetMapping("/edit/{id}")
    public String editReview(@PathVariable Integer id, Model model) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for ID: " + id));

        // フォームに既存のレビュー情報をセット
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setId(review.getId());
        reviewForm.setHouseId(review.getHouse().getId());
        reviewForm.setRating(review.getRating());
        reviewForm.setComment(review.getComment());

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("house", review.getHouse());

        return "reviews/edit"; 
    }

    @PostMapping("/edit/{id}")
    public String updateReview(@PathVariable Integer id, @ModelAttribute ReviewForm reviewForm) {
        // IDに基づいて既存のレビューを取得
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for ID: " + id));

        // フォームの値をセット
        review.setRating(reviewForm.getRating());
        review.setComment(reviewForm.getComment());

        // レビューを保存
        reviewService.save(review);

        return "redirect:/reservations"; // 更新後に予約一覧へ
    }
    
    @Transactional
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Integer id) {
        try {
            // レビュー削除前に関連する予約情報のレビューIDをnullに設定
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Review not found for ID: " + id));

            Reservation reservation = review.getReservation();
            if (reservation != null) {
                reservation.setReview(null); // レビューIDをnullにする
                reservationRepository.save(reservation); // 予約を保存
            }

            reviewRepository.deleteById(id); // レビューを削除
            reviewRepository.flush(); // 強制的にデータベースに反映

        } catch (Exception e) {
            e.printStackTrace();
            return "errorPage"; // エラー発生時にエラーページに遷移
        }
        return "redirect:/reservations"; // 削除後、予約一覧画面にリダイレクト
    }
}
