package com.example.ourLog.repository;

import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

  Optional<Answer> findByQuestion(Question question);

  // Question에 연결된 답변 삭제
  @Modifying
  @Query("delete from Answer a where a.question.questionId = :questionId")
  void deleteAnswersByQuestionId(@Param("questionId") Long questionId);
}
