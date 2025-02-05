package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // お気に入りの追加
    @PostMapping("/add")
    public String addFavorite(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam Integer houseId) {
    	User user = userDetails.getUser(); // ログイン中のユーザーを取得
        favoriteService.addFavorite(user, houseId);
        return "redirect:/houses/" + houseId;
    }

    // お気に入りの削除
    @PostMapping("/remove")
    public String removeFavorite(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam Integer houseId) {
    	User user = userDetails.getUser(); 
        favoriteService.removeFavorite(user, houseId);
        return "redirect:/houses/" + houseId;
    }

    // お気に入り一覧の表示
    @GetMapping("/list")
    public String showFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model,@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable) {
        User user = userDetails.getUser(); // ログイン中のユーザーを取得
        Page<House> favorites = favoriteService.getFavorites(user.getId(), pageable); // ユーザーのお気に入りを取得
        model.addAttribute("favorites", favorites); // お気に入りの民宿リストを渡す
        model.addAttribute("totalPages", favorites.getTotalPages()); // 総ページ数を渡す
        model.addAttribute("currentPage", favorites.getNumber()); // 現在のページ番号を渡す
        return "favorites/list"; // お気に入り一覧画面を返す
    }

}