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

  @Query("select fav FROM Favorite fav WHERE fav.userId = :userId")
  List<Favorite> findByUserId(@Param("userId") User userId);

  @Query("select fav FROM Favorite fav WHERE fav.userId = :userId AND fav.postId = :postId")
  Optional<Favorite> findByUserIdAndPostId(@Param("userId") User userId, @Param("postId") Post postId);

  @Query("select CASE WHEN COUNT(fav) > 0 THEN true ELSE false END FROM Favorite fav WHERE fav.userId = :userId AND fav.postId = :postId")
  boolean existsByUserIdAndPostId(@Param("userId") User userId, @Param("postId") Post postId);

  @Modifying
  @Transactional
  @Query("delete FROM Favorite fav WHERE fav.userId = :userId AND fav.postId = :postId")
  void deleteByUserIdAndPostId(@Param("userId") User userId, @Param("postId") Post postId);

  @Query("select COUNT(fav) FROM Favorite fav WHERE fav.postId = :postId AND fav.favorited = true")
  Long countByPostIdAndFavoritedTrue(@Param("postId") Post postId);



}