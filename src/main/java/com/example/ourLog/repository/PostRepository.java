package com.example.ourLog.repository;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.entity.*;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, SearchRepository {
  @Query("select po, count(distinct r) " +
          "from Post po left outer join Reply r " +
          "on r.post = po where po.user = :userId group by po ")
  Page<Object[]> getListPage(Pageable pageable, @Param("userId") User userId);

  @Query("select po, pi, count(distinct r) " +
          "from Post po " +
          "left outer join Picture pi   on pi.post = po " +
          "left outer join Reply r on r.post = po where po.user = :user group by po, pi ")
  Page<Object[]> getPostsWithPicturesByUser(Pageable pageable, @Param("userId") User userId);

  @Query(value = "SELECT po.postId, pi.picId, pi.picName, COUNT(r.replyId) " +
          "FROM Picture pi " +
          "LEFT OUTER JOIN Post po ON po.postId = pi.post.postId " +
          "LEFT OUTER JOIN Reply r ON po.postId = r.post.postId " +
          "WHERE pi.picId = (SELECT MAX(pi2.picId) FROM Picture pi2 WHERE pi2.post.postId = po.postId) " +
          "AND po.user.userId = :userId " +
          "GROUP BY po.postId, pi.picId, pi.picName", nativeQuery = true)

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

  @Query("select po, pi, u, count(distinct r), t from Post po " +
          "left outer join Picture pi on pi.post = po " +
          "left outer join Reply r on r.post = po " +
          "left outer join User u on po.user = u " +
          "left outer join Trade t on t.post = po " +
          "where po.postId = :postId " +
          "group by po, pi, u, t ")
//  @Query("SELECT po, pi, u, COUNT(r) FROM Post po " +
//          "LEFT JOIN po.pictures pi " +
//          "LEFT JOIN po.user u " +
//          "LEFT JOIN po.reply r " +
//          "WHERE po.postId = :postId " +
//          "GROUP BY po, pi, u")
  List<Object[]> getPostWithAll(@Param("postId") Long postId);

  @Query("SELECT p, pic, u FROM Post p " +
          "LEFT JOIN Picture pic ON pic.post = p " +
          "JOIN p.user u")
  List<Object[]> getAllPostsWithPicturesAndUser();

  // ✅ FETCH JOIN을 사용하여 Picture와 Trade를 즉시 로딩
  @Query("SELECT p, u FROM Post p " +
//      "LEFT JOIN FETCH Picture pic ON pic.post = p " + // FETCH JOIN 사용
      "JOIN p.user u " +
//      "LEFT JOIN FETCH Trade t ON t.post = p " + // FETCH JOIN 사용
      "WHERE (:boardNo IS NULL OR :boardNo = 0 OR p.boardNo = :boardNo) " +
      "AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
      "ORDER BY p.postId DESC")
  Page<Object[]> searchPage(
      @Param("boardNo") Long boardNo,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  // ✅ 추가: postId 목록으로 Picture 목록을 가져오는 쿼리
  @Query("SELECT p FROM Picture p WHERE p.post.postId IN :postIds")
  List<Picture> findPicturesByPostIds(@Param("postIds") List<Long> postIds);

  // ✅ 추가: postId 목록으로 Trade 목록을 가져오는 쿼리
  @Query("SELECT t FROM Trade t WHERE t.post.postId IN :postIds")
  List<Trade> findTradesByPostIds(@Param("postIds") List<Long> postIds);

//  @Query("SELECT po, pi, u, COUNT(r) " +
//          "FROM Post po " +
//          "LEFT JOIN po.pictures pi " +
//          "LEFT JOIN po.user u " +
//          "LEFT JOIN po.replies r " +
//          "GROUP BY po " +
//          "ORDER BY po.views DESC")
//  Page<PostDTO> getPopularPosts(Pageable pageable);

  @EntityGraph(attributePaths = {"user", "userProfile"}, type = EntityGraph.EntityGraphType.LOAD)
  List<Post> findAllByBoardNoOrderByViewsDesc(int boardNo);

  @EntityGraph(attributePaths = {"user", "userProfile"}, type = EntityGraph.EntityGraphType.LOAD)
  List<Post> findAllByBoardNoOrderByFollowersDesc(int boardNo);

  @EntityGraph(attributePaths = {"user", "userProfile"}, type = EntityGraph.EntityGraphType.LOAD)
  List<Post> findAllByBoardNoOrderByDownloadsDesc(int boardNo);

  List<Post> findByUser_UserId(Long userId);

  @Query("""
  SELECT p, pic, u FROM Post p
  LEFT JOIN Picture pic ON pic.post = p
  JOIN p.user u
  WHERE (:boardNo IS NULL OR :boardNo = 0 OR p.boardNo = :boardNo)
  AND (
    LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    p.content LIKE CONCAT('%', :keyword, '%') OR
    LOWER(p.tag) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
  )
  GROUP BY p, pic, u
  ORDER BY p.postId DESC
""")
  Page<Object[]> searchAllFields(
      @Param("boardNo") Long boardNo,
      @Param("keyword") String keyword,
      Pageable pageable
  );



}