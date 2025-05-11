package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;

public interface QuestionService {

  // DTO → Entity 변환 메서드
  default Question dtoToEntity(QuestionDTO questionDTO, User user) {
    return Question.builder()
            .questionId(questionDTO.getQuestionId())
            .title(questionDTO.getTitle())
            .content(questionDTO.getContent())
            .user(user)
            .isOpen(questionDTO.isOpen())  // 추가
            .build();
  }

  // Entity → DTO 변환 메서드
  default QuestionDTO entityToDto(Question question, UserDTO userDTO, AnswerDTO answerDTO) {
    // AnswerDTO 생성 (question.getAnswer()에 따른 변환)
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
            .answerDTO(answerDTO) // 답변 포함
            .isOpen(question.isOpen())  // ✅ 여기 수정됨!
            .build();
  }

  // Question 등록
  Long registerQuestion(QuestionDTO questionDTO);

  // Question 목록 조회
  PageResultDTO<QuestionDTO, Question> getQuestionList(PageRequestDTO requestDTO);

  // 단일 Question 조회
  QuestionDTO readQuestion(Long questionId, User user);

  // Question 수정
  void modifyQuestion(QuestionDTO questionDTO, User user);

  // Question 및 관련 댓글 삭제
  void deleteQuestion(Long questionId, User user);
}
