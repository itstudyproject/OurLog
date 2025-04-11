package com.example.ourLog.repository;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  // JPQL 이용해서 update, delete 실행할 때 적용
  @Modifying
  @Query("delete from Reply r where r.post.postId = :postId ")
  void deleteByPostId(@Param("postId") Long postId);

  // 쿼리메서드로 구성
  List<Reply> getRepliesByPostOrderByReplyId (Post post);

  @Modifying
  @Transactional
  @Query("delete from Reply r where r.qna.qnaId = :qnaId")
  void deleteByQnaId(@Param("qnaId") Long qnaId);
}
