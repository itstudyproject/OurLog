package com.example.ourLog.repository;

import com.example.ourLog.entity.QnA;
import com.example.ourLog.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
  Optional<QnaAnswer> findByQuestion(QnA question);

  // QnA에 연결된 답변 삭제
  @Modifying
  @Query("delete from QnaAnswer qa where qa.question.qnaId = :qnaId")
  void deleteQnAWithAnswer(@Param("qnaId") Long qnaId);
}