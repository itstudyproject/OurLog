package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Long> {

  @Modifying
  @Query("delete from Picture pi where pi.uuid=:uuid ")
  void deleteByUuid(@Param("uuid") String uuid);

  @Query("select pi from Picture pi where pi.post.postId=:postId")
  List<Picture> findByPostId(@Param("postId") Long postId);

  @Modifying
  @Query("delete from Picture pi where pi.post.postId=:postId")
  void deleteByPostId(@Param("postId") long postId);


  @Query("SELECT p FROM Picture p WHERE p.uuid = :uuid")
  Picture findByUuid(@Param("uuid") String uuid);
}