package com.example.samuraitravel.controller;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping("/houses")
public class HouseController {
	 private final HouseRepository houseRepository;
	 private final ReviewRepository reviewRepository;
	 private final FavoriteService favoriteService;
	 
	 public HouseController(HouseRepository houseRepository, ReviewRepository reviewRepository,FavoriteService favoriteService) {
		this.houseRepository = houseRepository;
		this.reviewRepository = reviewRepository;
		this.favoriteService = favoriteService;
	 }
	 
	 @GetMapping
	 public String index(@RequestParam(required = false) String keyword,
			 			 @RequestParam(required = false) String area,
			 			 @RequestParam(required = false) Integer price,
			 			 @RequestParam(required = false) String order,
			 			 @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model)
	 {
		 Page<House> housePage;
		 
		 if(keyword != null && !keyword.isEmpty()) {
			 if(order != null && order.equals("priceAsc")) {
				 housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%", "%" + keyword + "%", pageable);
			 } else {
				 housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%", "%" + keyword + "%", pageable);
			 }
		 }else if(area != null && !area.isEmpty()) {
			 if(order != null && order.equals("priceAsc")) {
				 housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
			 } else {
				 housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
			 }
		 }else if(price != null) {
			 if(order != null && order.equals("priceAsc")) {
				 housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
			 } else {
				 housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			 }
		 }else {
			 if(order != null && order.equals("priceAsc")) {
				 housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
			 }else {
				 housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);
			 }
		 }
			 
		 model.addAttribute("housePage",housePage);
		 model.addAttribute("keyword",keyword);
		 model.addAttribute("area",area);
		 model.addAttribute("price",price);
		 model.addAttribute("order",order);
		 
		 return "houses/index";
	 }
	 
	 @GetMapping("/{id}")
	 public String show(
	         @PathVariable Integer id,
	         Model model,
	         @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
	         Authentication authentication) {

	     // 民宿情報を取得（存在しない場合は例外スロー）
	     House house = houseRepository.findById(id)
	             .orElseThrow(() -> new EntityNotFoundException("Invalid house ID: " + id));

	     // ページング可能なレビュー一覧を取得
	     Page<Review> reviewPage = reviewRepository.findByHouse(house, pageable);

	     // ログインしている場合にお気に入りの状態をチェック
	     if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
	         UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	         User user = userDetails.getUser();

	         // お気に入りの状態をチェック
	         boolean isFavorite = favoriteService.isFavorite(user, house);
	         model.addAttribute("isFavorite", isFavorite); // モデルにお気に入り情報を追加
	     }

	     // モデルにデータを追加
	     model.addAttribute("house", house);
	     model.addAttribute("reviews", reviewPage.getContent()); // レビューのリスト
	     model.addAttribute("reviewPage", reviewPage); // ページ情報（ページネーション用）
	     model.addAttribute("totalReviews", reviewPage.getTotalElements()); // レビューの総数
	     model.addAttribute("reservationInputForm", new ReservationInputForm());

	     return "houses/show"; // 詳細画面のテンプレート
	 }

}
