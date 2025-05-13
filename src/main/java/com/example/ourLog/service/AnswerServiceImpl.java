package com.example.ourLog.service;

import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;

  public Answer writeAnswer(Long questionId, String contents, User writer) {
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Question 없음"));

    // 이미 답변이 있는 경우 예외 처리
    answerRepository.findByQuestion(question)
            .ifPresent(answer -> {
              throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
            });

    Answer answer = new Answer(writer, question, contents);
    return answerRepository.save(answer);
  }

  public void modifyAnswer(Long answerId, String newContents, User loginUser) throws IllegalAccessException {
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(loginUser)) {
      throw new IllegalAccessException("수정 권한이 없습니다.");
    }

    answer.updateContents(newContents);
    answerRepository.save(answer);
  }

  public void deleteAnswer(Long answerId, User loginUser) throws IllegalAccessException {
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(loginUser)) {
      throw new IllegalAccessException("삭제 권한이 없습니다.");
    }

    answerRepository.delete(answer);
  }
}