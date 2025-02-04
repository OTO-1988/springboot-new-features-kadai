package com.example.samuraitravel.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.UserRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final HouseRepository houseRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, HouseRepository houseRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
    }

    // お気に入りに追加
    public void addFavorite(User user, Integer houseId) {
        user = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        House house = houseRepository.findById(houseId).orElseThrow(() -> new IllegalArgumentException("House not found"));
        
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setHouse(house);
        favoriteRepository.save(favorite);
    }

    // お気に入りから削除
    public void removeFavorite(User user, Integer houseId) {
        user = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        House house = houseRepository.findById(houseId).orElseThrow(() -> new IllegalArgumentException("House not found"));
        
        Favorite favorite = favoriteRepository.findFirstByUserAndHouse(user, house).orElseThrow(() -> new IllegalArgumentException("Favorite not found"));
        favoriteRepository.delete(favorite);
    }

    // 会員のお気に入り一覧を取得
    public Page<House> getFavorites(Integer userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // お気に入りをページネーションで取得し、House を取り出して返す
        Page<Favorite> favoritesPage = favoriteRepository.findByUser(user, pageable);
        
        // お気に入りから House オブジェクトを取り出す
        return favoritesPage.map(Favorite::getHouse);
    }

    // 会員がすでにお気に入りに追加しているか確認
    public boolean isFavorite(User user, House house) {
        return favoriteRepository.existsByUserAndHouse(user, house);
    }
}