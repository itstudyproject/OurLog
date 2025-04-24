package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Long> {

  @Query("SELECT p FROM Picture p WHERE p.uuid = :uuid ")
  Picture findByUuid(@Param("uuid") String uuid);

  @Query("SELECT p FROM Picture p WHERE p.post = :postId ")
  List<Picture> findByPostId(@Param("postId") Long postId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Picture p WHERE p.uuid = :uuid ")
  void deleteByUuid(@Param("uuid") String uuid);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Picture p WHERE p.post = :postId ")
  void deleteByPostId(@Param("postId") Long postId);

}
