package com.example.ourLog.repository;

import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AnswerRepositoryTests {

  @Autowired
  AnswerRepository answerRepository;

  @Autowired
  QuestionRepository questionRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @Transactional
  @Commit
  public void insertQuestionAnswer() {
    // 1) 운영자 User 객체를 꺼내온다 (ID 1번이 운영자라고 가정)
    User admin = userRepository.findById(1L)
            .orElseThrow(() -> new IllegalStateException("운영자 유저가 없습니다"));

    IntStream.rangeClosed(1, 100).forEach(i -> {
      Long questionId = (long) i;
      questionRepository.findById(questionId).ifPresent(q -> {
        // 한 질문당 하나씩만 달릴 수 있는 OneToOne 구조라면 이미 없는 경우에만
        if (answerRepository.findByQuestion(q).isEmpty()) {
          Answer answer = Answer.builder()
                  .user(admin)                  // 여기가 핵심!
                  .question(q)
                  .contents("문의 답변 내용 " + i)
                  .build();
          answerRepository.save(answer);
        }
      });
    });
  }
}