package com.example.ourLog.repository;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {


  @Query("select fav FROM Favorite fav WHERE fav.userId = :userId ")
  List<Favorite> findByUser(@Param("userId") Long userId);

  @Query("SELECT fav FROM Favorite fav WHERE fav.post = :post")
  List<Favorite> findByPost(@Param("post") Post post);

  @Query("select fav FROM Favorite fav WHERE fav.user = :user AND fav.post = :post ")
  Optional<Favorite> findByUserAndPost(@Param("user") User user, @Param("post") Post post);

  @Query("select CASE WHEN COUNT(fav) > 0 THEN true ELSE false END " +
      "FROM Favorite fav WHERE fav.user = :user AND fav.post = :post ")
  boolean existsByUserAndPost(@Param("user") User user, @Param("post") Post post);

  @Modifying
  @Transactional
  @Query("delete FROM Favorite fav WHERE fav.user = :user AND fav.post = :post ")
  void deleteByUserAndPost(@Param("user") User user, @Param("post") Post post);

  @Query("select COUNT(fav) FROM Favorite fav WHERE fav.post = :post AND fav.favorited = true ")
  Long countByPostAndFavoritedTrue(@Param("post") Post post);

//  @Query("select COUNT(fav) FROM Favorite fav WHERE fav.post = :post ")
//  Long countFavoritesByPost(@Param("post") Post post);
}