package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionRepositoryTests {
  @Autowired
  private QuestionRepository questionRepository;
  
  @Test
  @Transactional
  @Commit
  public void insertQuestions() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      Question question = Question.builder()
              .title("문의Title..." + i)
              .content("문의Content..." + i)
              .user(User.builder().userId(Long.valueOf(i)).build())
              .build();
      questionRepository.save(question);
    });
  }
}