package com.example.ourLog.repository;

import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long>, SearchRepository {

  @Query("select q, a from Question q " +
          "left join Answer a on a.question = q " +
          "where q.questionId = :questionId and a.user = :user ")
  List<Object[]> getQuestionWithAnswer(@Param("questionId") Long questionId,
                                       @Param("User") User user);

  @Modifying(clearAutomatically = true)
  @Query("delete from Answer a where a.question.questionId = :questionId ")
  void deleteByQuestionId(@Param("questionId") Long questionId);
}