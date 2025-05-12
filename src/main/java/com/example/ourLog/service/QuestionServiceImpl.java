package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QuestionRepository;
import com.example.ourLog.repository.AnswerRepository;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionServiceImpl implements QuestionService {

  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;
  private final UserRepository userRepository;

  @Override
  public Long inquiry(QuestionDTO questionDTO) {
    User user = User.builder()
            .userId(questionDTO.getUserDTO().getUserId())
            .email(questionDTO.getUserDTO().getEmail())
            .nickname(questionDTO.getUserDTO().getNickname())
            .build();

    Question question = dtoToEntity(questionDTO, user);
    questionRepository.save(question);
    return question.getQuestionId();
  }

  @Override
  public PageResultDTO<QuestionDTO, Question> getQuestionList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("regDate").descending());
    Page<Question> result = questionRepository.getQuestionList(pageable);

    Function<Question, QuestionDTO> fn = question -> {
      User user = question.getUser();
      UserDTO userDTO = UserDTO.builder()
              .userId(user.getUserId())
              .email(user.getEmail())
              .nickname(user.getNickname())
              .build();

      Answer answer = question.getAnswer();
      AnswerDTO answerDTO = null;
      if (answer != null) {
        answerDTO = AnswerDTO.builder()
                .answerId(answer.getAnswerId())
                .contents(answer.getContents())
                .regDate(answer.getRegDate())
                .modDate(answer.getModDate())
                .build();
      }

      return entityToDto(question, userDTO, answerDTO);
    };

    return new PageResultDTO<>(result, fn);
  }

  @Override
  public List<QuestionDTO> getQuestionsByUserEmail(String userEmail) {
    User user = userRepository.findByNickname(userEmail)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    List<Question> questions = questionRepository.findByUser(user);

    return questions.stream()
            .map(question -> {
              UserDTO userDTO = UserDTO.builder()
                      .userId(user.getUserId())
                      .email(user.getEmail())
                      .nickname(user.getNickname())
                      .build();

              Answer answer = question.getAnswer();
              AnswerDTO answerDTO = null;
              if (answer != null) {
                answerDTO = AnswerDTO.builder()
                        .answerId(answer.getAnswerId())
                        .contents(answer.getContents())
                        .regDate(answer.getRegDate())
                        .modDate(answer.getModDate())
                        .build();
              }

              return entityToDto(question, userDTO, answerDTO);
            })
            .collect(Collectors.toList());
  }

  @Override
  public QuestionDTO readQuestion(Long questionId, UserAuthDTO user) {
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
  public void editingInquiry(QuestionDTO questionDTO, UserAuthDTO user) {
    Question question = questionRepository.findQuestionById(questionDTO.getQuestionId())
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
  public void deleteQuestion(Long questionId, UserAuthDTO user) {
    Question question = questionRepository.findQuestionById(questionId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 질문입니다."));

    if (!question.getUser().getUserId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 질문만 삭제할 수 있습니다.");
    }

    answerRepository.deleteQuestionWithAnswer(questionId);
    questionRepository.deleteByQuestionId(questionId);
  }
}
