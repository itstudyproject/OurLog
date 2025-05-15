package com.example.ourLog.service;

import com.example.ourLog.dto.AnswerDTO;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.AnswerRepository;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AnswerServiceImpl implements AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final UserRepository userRepository;

  @Override
  public AnswerDTO writeAnswer(Long questionId, String contents, UserAuthDTO userAuthDTO) {
    log.info("writeAnswer 호출됨 - questionId: {}, contents: {}, userEmail: {}", questionId, contents, userAuthDTO.getEmail());

    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> {
              log.error("해당 Question 없음 - questionId: {}", questionId);
              return new IllegalArgumentException("해당 Question 없음");
            });

    answerRepository.findByQuestion(question)
            .ifPresent(answer -> {
              log.warn("이미 답변이 존재함 - questionId: {}", questionId);
              throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
            });

    User user = userRepository.findByEmail(userAuthDTO.getEmail())
            .orElseThrow(() -> {
              log.error("유저를 찾을 수 없음 - email: {}", userAuthDTO.getEmail());
              return new IllegalArgumentException("유저를 찾을 수 없습니다.");
            });

    Answer answer = new Answer(user, question, contents);
    Answer saved = answerRepository.save(answer);
    log.info("답변 저장 완료 - answerId: {}", saved.getAnswerId());

    // 인터페이스의 default entityToDto() 메서드 사용
    return entityToDto(saved);
  }

  @Transactional
  @Override
  public void modifyAnswer(Long answerId, String newContents, UserAuthDTO userAuthDTO) {
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(userAuthDTO)) {
      throw new AccessDeniedException("수정 권한이 없습니다.");
    }

    answer.updateContents(newContents);
    answerRepository.save(answer);
    log.info("답변 수정 완료 - answerId: {}", answerId);
  }

  @Transactional
  @Override
  public void deleteAnswer(Long answerId, UserAuthDTO userAuthDTO) {
    Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(userAuthDTO)) {
      throw new AccessDeniedException("삭제 권한이 없습니다.");
    }

    answerRepository.delete(answer);
    log.info("답변 삭제 완료 - answerId: {}", answerId);
  }
}