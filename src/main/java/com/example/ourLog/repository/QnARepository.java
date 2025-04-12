package com.example.ourLog.repository;

import com.example.ourLog.entity.QnA;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QnARepository extends JpaRepository<QnA, Long>, SearchRepository {

  @Query("select q, u, count(r) " +
          "from QnA q " +
          "left outer join Reply r on r.qna = q " +
          "left outer join User u on q.writer = u " +
          "where q.qnaId = :qnaId")
  List<Object[]> getQnAWithAll(@Param("qnaId") Long qnaId);

  @Modifying
  @Query("delete from Reply r where r.qna.qnaId = :qnaId")
  void deleteByQnaId(@Param("qnaId") Long qnaId);
}
