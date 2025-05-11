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
import org.springframework.data.domain.Pageable;
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
  public PageResultDTO<QuestionDTO, Question> getQuestionList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("regDate").descending());
    Page<Question> result = questionRepository.getQuestionList(pageable);
    Function<Question, QuestionDTO> fn = (q) -> questionToDTO(q); // questionToDTO 호출
    return new PageResultDTO<>(result, fn);
  }

  private QuestionDTO questionToDTO(Question question) {
    // UserDTO 생성 (필요한 필드만 가져옴)
    UserDTO userDTO = UserDTO.builder()
            .userId(question.getUser().getUserId())
            .nickname(question.getUser().getNickname())  // nickname 사용
            .email(question.getUser().getEmail())        // email 사용
            .build();

    // AnswerDTO 생성 (null 체크 후)
    AnswerDTO answerDTO = null;
    if (question.getAnswer() != null) {
      answerDTO = new AnswerDTO(
              question.getAnswer().getAnswerId(),
              question.getAnswer().getContents(),
              question.getAnswer().getRegDate(),
              question.getAnswer().getModDate()
      );
    }

    // QuestionDTO 생성
    return QuestionDTO.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            .content(question.getContent())
            .userDTO(userDTO)
            .regDate(question.getRegDate())
            .modDate(question.getModDate())
            .answerDTO(answerDTO) // 답변 포함
            .build();
  }

  @Override
  public QuestionDTO readQuestion(Long questionId, User user) {
    Object[] result = questionRepository.getQuestionWithAnswer(questionId, user)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("질문이 존재하지 않거나 접근 권한이 없습니다."));

    Question q = (Question) result[0];  // Question
    UserDTO userDTO = (UserDTO) result[1];  // UserDTO
    AnswerDTO answerDTO = (AnswerDTO) result[2];  // AnswerDTO

    if (!q.getUser().getUserId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 질문만 조회할 수 있습니다.");
    }

    return entityToDto(q, userDTO, answerDTO);  // AnswerDTO를 반환하도록 수정
  }

  @Override
  public void modifyQuestion(QuestionDTO questionDTO, User user) {
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
