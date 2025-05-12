package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;

public interface QuestionService {

  // DTO → Entity 변환 메서드
  default Question dtoToEntity(QuestionDTO questionDTO, User user) {
    return Question.builder()
            .questionId(questionDTO.getQuestionId())
            .title(questionDTO.getTitle())
            .content(questionDTO.getContent())
            .user(user)
            .build();
  }

  // Entity → DTO 변환 메서드
  default QuestionDTO entityToDto(Question question, UserDTO userDTO, AnswerDTO answerDTO) {
    return QuestionDTO.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            .content(question.getContent())
            .userDTO(userDTO)
            .regDate(question.getRegDate())
            .modDate(question.getModDate())
            .answerDTO(answerDTO)
            .build();
  }

  // Question 등록
  Long registerQuestion(QuestionDTO questionDTO);

  // 페이징된 Question 목록 조회
  PageResultDTO<QuestionDTO, Object[]> listQuestion(PageRequestDTO pageRequestDTO);

  // 단일 Question 조회
  QuestionDTO readQuestion(Long questionId, User user);

  // Question 수정
  void modifyQuestion(QuestionDTO questionDTO, User user);

  // Question 및 관련 댓글 삭제
  void deleteQuestion(Long questionId, User user);
}
