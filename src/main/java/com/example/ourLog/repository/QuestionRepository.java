package com.example.ourLog.repository;

import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface QuestionRepository extends JpaRepository<Question, Long>, SearchRepository {

  @Query("select q from Question q where q.questionId = :questionId")
  Optional<Question> findQuestionById(@Param("questionId") Long questionId);

  @Query("select q, a from Question q " +
          "left join Answer a on a.question = q " +
          "where q.questionId = :questionId and a.user = :user ")
  List<Object[]> getQuestionWithAnswer(@Param("questionId") Long questionId,
                                       @Param("user") User user);

  @Query("select q from Question q")
  Page<Question> getQuestionList(Pageable pageable);

  @Modifying
  @Query("delete from Answer a where a.question.questionId = :questionId")
  void deleteByQuestionId(@Param("questionId") Long questionId);

  List<Question> findByUser(User user);
}