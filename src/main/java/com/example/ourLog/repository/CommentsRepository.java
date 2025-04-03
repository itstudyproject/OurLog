package com.example.ourLog.repository;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  // JPQL 이용해서 update, delete 실행할 때 적용
  @Modifying
  @Query("delete from Reply r where r.board.bno = :bno ")
  void deleteByBno(@Param("bno") Long bno);

  // 쿼리메서드로 구성
  List<Reply> getRepliesByBoardOrderByRno (Post post);
}
