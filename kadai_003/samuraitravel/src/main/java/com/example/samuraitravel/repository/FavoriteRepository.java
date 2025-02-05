package com.example.samuraitravel.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findFirstByUserAndHouse(User user, House house);


    Page<Favorite> findByUser(User user, Pageable pageabel);
    
    public boolean  existsByUserAndHouse(User user, House house);
    
    Favorite findByUserAndHouse(User user, House house);
}