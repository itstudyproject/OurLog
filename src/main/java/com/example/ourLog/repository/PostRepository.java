package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
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
      "on r.post = po where po.user.userId = :userId group by po ")
  Page<Object[]> getListPage(Pageable pageable, @Param("userId") Long userId);

  @Query("select po, pi, count(distinct r) " +
      "from Post po " +
      "left outer join Picture pi   on pi.post = po " +
      "left outer join Reply r on r.post = po where po.user.userId = :userId group by po ")
  Page<Object[]> getPostsWithPicturesByUser(Pageable pageable, @Param("userId") Long userId);

  @Query(value = "SELECT po.postId, pi.picId, pi.picName, COUNT(r.replyId) " +
      "FROM picture pi " +
      "LEFT OUTER JOIN post po ON po.postId = pi.post_postId " +
      "LEFT OUTER JOIN comments r ON po.postId = r.post_postId " +
      "WHERE pi.picId = (SELECT MAX(picId) FROM picture pi2 WHERE pi2.post_postId = po.postId) " +
      "AND po.user_userId = :userId " +
      "GROUP BY po.postId", nativeQuery = true)
  Page<Object[]> getLatestPictureNativeByUser(Pageable pageable, @Param("userId") Long userId);

  @Query("SELECT po, pi, COUNT(DISTINCT r) FROM Post po " +
      "LEFT JOIN Picture pi ON pi.post = po " +
      "LEFT JOIN Reply r ON r.post = po " +
      "WHERE pi.picId = (" +
      "  SELECT MAX(pi2.picId) FROM Picture pi2 WHERE pi2.post = po" +
      ") AND po.user.userId = :userId " +
      "GROUP BY po, pi")
  Page<Object[]> getListPageLatestPicture(Pageable pageable, @Param("userId") Long userId);


  @Query("SELECT pi FROM Picture pi WHERE pi.picId = (" +
      "SELECT MAX(pi2.picId) FROM Picture pi2 WHERE pi2.post = pi.post" +
      ")")
  List<Picture> findLatestPicturesPerPost();

  @Query("SELECT po, pi, u, COUNT(r) FROM Post po " +
      "LEFT JOIN Picture pi ON pi.postId = po " +
      "LEFT JOIN User u ON po.userId = u " +
      "LEFT JOIN Reply r ON r.post = po " +
      "WHERE po.postId = :postId GROUP BY po, pi, u")
  List<Object[]> getPostWithAll(@Param("postId") Long postId);

  @Query("SELECT po, pi, u, COUNT(r) " +
      "FROM Post po " +
      "LEFT JOIN Picture pi ON pi.postId = po " +
      "LEFT JOIN User u ON po.userId = u " +
      "LEFT JOIN Reply r ON r.post = po " +
      "GROUP BY po " +
      "ORDER BY po.views DESC")
  Page<Object[]> getPopularPosts(Pageable pageable);


}