package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.AnswerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionServiceImpl implements QuestionService {
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;

  @Override
  public Long registerQuestion(QuestionDTO questionDTO) {
    User writer = User.builder()
            .userId(questionDTO.getUserDTO().getUserId())
            .nickname(questionDTO.getUserDTO().getNickname())
            .build();

    Question question = dtoToEntity(questionDTO, writer);
    questionRepository.save(question);
    return question.getQuestionId();
  }

  @Override
  public PageResultDTO<QuestionDTO, Object[]> listQuestion(PageRequestDTO pageRequestDTO) {
    log.info(">>" + pageRequestDTO);

    Page<Object[]> result = questionRepository.searchPage(
            pageRequestDTO.getType(),
            pageRequestDTO.getKeyword(),
            pageRequestDTO.getPageable(Sort.by("questionId").descending())
    );

    Function<Object[], QuestionDTO> fn = (arr) ->
            entityToDto((Question) arr[0], (UserDTO) arr[1], (AnswerDTO) arr[2]);

    return new PageResultDTO<>(result, fn);
  }

  @Override
  public QuestionDTO readQuestion(Long questionId, User user) {
    Object[] result = questionRepository.getQuestionWithAnswer(questionId, user)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("질문이 존재하지 않거나 접근 권한이 없습니다."));

    Question q = (Question) result[0];
    UserDTO userDTO = (UserDTO) result[1];
    AnswerDTO answerDTO = (AnswerDTO) result[2];

    if (!q.getUser().getUserId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 질문만 조회할 수 있습니다.");
    }

    return entityToDto(q, userDTO, answerDTO);
  }

  @Override
  public void modifyQuestion(QuestionDTO questionDTO, User user) {
    Question question = questionRepository.findById(questionDTO.getQuestionId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 질문입니다."));

    if (!question.getUser().getUserId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 질문만 수정할 수 있습니다.");
    }

    question.changeQuestionTitle(questionDTO.getTitle());
    question.changeQuestionContent(questionDTO.getContent());
    questionRepository.save(question);
  }

  @Transactional
  @Override
  public void deleteQuestion(Long questionId, User user) {
    Question question = questionRepository.findQuestionById(questionId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 질문입니다."));

    if (!question.getUser().getUserId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 질문만 삭제할 수 있습니다.");
    }

    answerRepository.deleteQuestionWithAnswer(questionId);  // Answer 삭제
    questionRepository.deleteByQuestionId(questionId);      // Question 삭제
  }
}
