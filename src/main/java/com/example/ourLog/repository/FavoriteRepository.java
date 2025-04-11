package com.example.ourLog.repository;

import com.example.ourLog.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  List<Favorite> findByUserId(Long userId);

  Optional<Favorite> findByUserIdAndImageId(Long userId, Long imageId);

  void deleteByUserIdAndImageId(Long userId, Long imageId);

  boolean existsByUserIdAndImageId(Long userId, Long imageId);
}