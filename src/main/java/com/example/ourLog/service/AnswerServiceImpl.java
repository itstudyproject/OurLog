package com.example.ourLog.service;

import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.AnswerRepository;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final UserRepository userRepository;

  public Answer writeAnswer(Long questionId, String contents, UserAuthDTO userAuthDTO) {
    // 해당 Question이 있는지 확인
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Question 없음"));

    // 이미 답변이 있는 경우 예외 처리
    answerRepository.findByQuestion(question)
            .ifPresent(answer -> {
              throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
            });

    // UserAuthDTO에서 User 엔티티로 변환
    User user = userRepository.findByEmail(userAuthDTO.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    // Answer 객체 생성 및 저장
    Answer answer = new Answer(user, question, contents);
    return answerRepository.save(answer);
  }

  public void modifyAnswer(Long answerId, String newContents, UserAuthDTO userAuthDTO) throws IllegalAccessException {
    // 해당 답변이 존재하는지 확인
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    // 작성자가 동일한지 확인
    if (!answer.isSameWriter(userAuthDTO)) {
      throw new IllegalAccessException("수정 권한이 없습니다.");
    }

    // 내용 수정
    answer.updateContents(newContents);
    answerRepository.save(answer);
  }

  public void deleteAnswer(Long answerId, UserAuthDTO userAuthDTO) throws IllegalAccessException {
    // 해당 답변이 존재하는지 확인
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    // 작성자가 동일한지 확인
    if (!answer.isSameWriter(userAuthDTO)) {
      throw new IllegalAccessException("삭제 권한이 없습니다.");
    }

    // 답변 삭제
    answerRepository.delete(answer);
  }
}