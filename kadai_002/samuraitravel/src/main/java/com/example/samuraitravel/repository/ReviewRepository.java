package com.example.samuraitravel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // ページング付きでレビューを取得（既存）
    Page<Review> findByHouseOrderByCreatedAtDesc(House houseId, Pageable pageable);
    Page<Review> findByHouse(House house,Pageable pageable);
    
    // 全てのレビューを取得（新規追加）
    List<Review> findByHouseOrderByCreatedAtDesc(House house);
    List<Review> findByHouse(House house);
    
    
 // 予約IDを使用してレビューの存在を確認する（追加）
    boolean existsByReservation(Reservation reservationId);
    
    Optional<Review> findByReservation(Reservation reservation);
    
    Optional<Review> findByReservationId(Integer reservationId);
}