package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.AnswerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionServiceImpl implements QuestionService {
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;

  @Override
  public Long register(QuestionDTO questionDTO) {
    User writer = User.builder()
            .userId(questionDTO.getUserDTO().getUserId())
            .name(questionDTO.getUserDTO().getName())
            .build();

    Question question = dtoToEntity(questionDTO, writer);
    questionRepository.save(question);
    return question.getQuestionId();
  }

  @Override
  public PageResultDTO<QuestionDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {

    log.info(">>" + pageRequestDTO);

    Page<Object[]> result = questionRepository.searchPage(
            pageRequestDTO.getType(),
            pageRequestDTO.getKeyword(),
            pageRequestDTO.getPageable(Sort.by("questionId").descending())
    );

    Function<Object[], QuestionDTO> fn = new Function<Object[], QuestionDTO>() {
      @Override
      public QuestionDTO apply(Object[] arr) {
        return entityToDto((Question) arr[0], (UserDTO) arr[1], (AnswerDTO) arr[2]);
      }
    };
    return new PageResultDTO<>(result, fn);
  }

  @Override
  public QuestionDTO get(Long questionId) {
    Object[] result = questionRepository.getQuestionWithAnswer(questionId).get(0);

      Question q = (Question) result[0];
      UserDTO userDTO = (UserDTO) result[1];
      AnswerDTO answerDTO = (AnswerDTO) result[2];

      return entityToDto(q, userDTO, answerDTO);
  }

  @Override
  public void modify(QuestionDTO questionDTO) {
    Optional<Question> result = questionRepository.findById(questionDTO.getQuestionId());
    if (result.isPresent()) {
      Question question = result.get();
      question.changeQuestionTitle(questionDTO.getTitle());
      question.changeQuestionContent(questionDTO.getContent());
      questionRepository.save(question);
    }

  }

  @Transactional
  @Override
  public void removeWithAnswer(Long questionId) {
    answerRepository.deleteQuestionWithAnswer(questionId);  // Answer 삭제
    questionRepository.deleteByQuestionId(questionId);
  }
}
