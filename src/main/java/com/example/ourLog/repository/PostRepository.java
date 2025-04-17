package com.example.ourLog.repository;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, SearchRepository {
  @Query("select po, count(distinct r) " +
      "from Post po left outer join Reply r " +
      "on r.post = po where po.userId = :userId group by po ")
  Page<Object[]> getListPage(Pageable pageable, @Param("userId") User userId);

  @Query("select po, pi, count(distinct r) " +
      "from Post po " +
      "left outer join Picture pi   on pi.post = po " +
      "left outer join Reply r on r.post = po where po.userId = :userId group by po ")
  Page<Object[]> getPostsWithPicturesByUser(Pageable pageable, @Param("userId") User userId);

  @Query(value = "SELECT po.postId, pi.picId, pi.picName, COUNT(r.replyId) " +
      "FROM picture pi " +
      "LEFT OUTER JOIN Post po ON po.postId = pi.post_postId " +
      "LEFT OUTER JOIN Reply r ON po.postId = r.post_postId " +
      "WHERE pi.picId = (SELECT MAX(picId) FROM picture pi2 WHERE pi2.post_postId = po.postId) " +
      "AND po.user_userId = :userId " +
      "GROUP BY po.postId", nativeQuery = true)
  Page<Object[]> getLatestPictureNativeByUser(Pageable pageable, @Param("userId") User userId);

  @Query("SELECT po, pi, COUNT(DISTINCT r) FROM Post po " +
      "LEFT JOIN Picture pi ON pi.post = po " +
      "LEFT JOIN Reply r ON r.post = po " +
      "WHERE pi.picId = (" +
      "  SELECT MAX(pi2.picId) FROM Picture pi2 WHERE pi2.post = po" +
      ") AND po.user.userId = :userId " +
      "GROUP BY po, pi")
  Page<Object[]> getListPageLatestPicture(Pageable pageable, @Param("userId") User userId);


  @Query("SELECT pi FROM Picture pi WHERE pi.picId = (" +
      "SELECT MAX(pi2.picId) FROM Picture pi2 WHERE pi2.post = pi.post" +
      ")")
  List<Picture> findLatestPicturesPerPost();

  @Query("select po, pi, u, count(r) from Post po " +
          "left outer join Pictures pi on pi.postId=po " +
          "left outer join Reply r on r.postId=po " +
          "left outer join Users u on po.userId=u " +
          "where po.postId = :postId group by po, pi, u")
//  @Query("SELECT po, pi, u, COUNT(r) FROM Post po " +
//          "LEFT JOIN po.pictures pi " +
//          "LEFT JOIN po.user u " +
//          "LEFT JOIN po.reply r " +
//          "WHERE po.postId = :postId " +
//          "GROUP BY po, pi, u")
  List<Object[]> getPostWithAll(@Param("postId") Long postId);


//  @Query("SELECT po, pi, u, COUNT(r) " +
//          "FROM Post po " +
//          "LEFT JOIN po.pictures pi " +
//          "LEFT JOIN po.user u " +
//          "LEFT JOIN po.replies r " +
//          "GROUP BY po " +
//          "ORDER BY po.views DESC")
//  Page<PostDTO> getPopularPosts(Pageable pageable);



}