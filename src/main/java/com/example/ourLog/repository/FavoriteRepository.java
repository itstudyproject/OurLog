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

  @Query("SELECT f FROM Favorite f WHERE f.userId = :user")
  List<Favorite> findByUserId(@Param("user") User user);

  @Query("SELECT f FROM Favorite f WHERE f.userId = :user AND f.postId = :post")
  Optional<Favorite> findByUserIdAndPostId(@Param("user") User user, @Param("post") Post post);

  @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.userId = :user AND f.postId = :post")
  boolean existsByUserIdAndPostId(@Param("user") User user, @Param("post") Post post);

  @Modifying
  @Transactional
  @Query("DELETE FROM Favorite f WHERE f.userId = :user AND f.postId = :post")
  void deleteByUserIdAndPostId(@Param("user") User user, @Param("post") Post post);

  Long countByPostIdAndIsFavoritedTrue(Post post);
}