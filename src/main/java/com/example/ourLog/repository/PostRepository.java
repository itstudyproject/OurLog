package com.example.ourLog.repository;

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
  Page<Object[]> getListPagePhotos(Pageable pageable, @Param("userId") Long userId);

  @Query(value = "select po.postId, pi.picId, pi.picName, " +
          "count(r.replyId) " +
          "from db7.picture pi left outer join db7.post po on po.postId=pi.post_postId " +
          "left outer join db7.comments c on po.postId=c.post_postId " +
          "where pi.picId = " +
          "(select max(picId) from db7.picture pi2 where pi2.post_postId=po.postId) " +
          "and po.user_userId = :userId " +
          "group by po.postId ", nativeQuery = true)
  Page<Object[]> getListPagePhotosNative(Pageable pageable, @Param("userId") Long userId);

  @Query("select po, pi, count(distinct r) from Post po " +
          "left outer join Picture p   on p.post = po " +
          "left outer join Reply r on r.post = po " +
          "where picId = (select max(p2.pno) from Picture pi2 where pi2.post=po) " +
          "and po.user.userId = :userId " +
          "group by po ")
  Page<Object[]> getListPagePhotosJPQL(Pageable pageable, @Param("userId") Long userId);

  @Query("select post, max(pi.picId) from Picture pi group by post")
  Page<Object[]> getMaxQuery(Pageable pageable);

  @Query("select po, pi, u, count(r) " +
          "from Post po left outer join Picture pi on pi.post=po " +
          "left outer join Reply r on r.post = po " +
          "left outer join User m on po.user = u " +
          "where po.postId = :postId group by pi ")
  List<Object[]> getJournalWithAll(@Param("postId") Long postId);

}
