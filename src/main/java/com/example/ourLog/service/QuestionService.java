package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;

import java.util.List;

public interface QuestionService {

  // DTO → Entity 변환 메서드
  default Question dtoToEntity(QuestionDTO questionDTO, User user) {
    return Question.builder()
            .questionId(questionDTO.getQuestionId())
            .title(questionDTO.getTitle())
            .content(questionDTO.getContent())
            .user(user)
            .isOpen(questionDTO.isOpen())
            .build();
  }

  // Entity → DTO 변환 메서드
  default QuestionDTO entityToDto(Question question, UserDTO userDTO, AnswerDTO answerDTO) {
    if (question.getAnswer() != null && answerDTO == null) {
      Answer answer = question.getAnswer();
      answerDTO = AnswerDTO.builder()
              .answerId(answer.getAnswerId())
              .contents(answer.getContents())
              .regDate(answer.getRegDate())
              .modDate(answer.getModDate())
              .build();
    }

    return QuestionDTO.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            .content(question.getContent())
            .userDTO(userDTO)
            .regDate(question.getRegDate())
            .modDate(question.getModDate())
            .isOpen(question.isOpen())
            .answerDTO(answerDTO)
            .build();
  }

  // Question 등록
  Long inquiry(QuestionDTO questionDTO);

  // 전체 목록 조회
  PageResultDTO<QuestionDTO, Question> getQuestionList(PageRequestDTO requestDTO);

  // 사용자 닉네임으로 목록 조회
  List<QuestionDTO> getQuestionsByUserNickname(String nickname);

  // 단일 조회
  QuestionDTO readQuestion(Long questionId, User user);

  // 수정
  void editingInquiry(QuestionDTO questionDTO, User user);

  // 삭제
  void deleteQuestion(Long questionId, User user);
}
