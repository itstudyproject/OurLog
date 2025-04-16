package com.example.ourLog.service;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;

public interface QuestionService {

  // DTO → Entity 변환 메서드
  default Question dtoToEntity(QuestionDTO questionDTO, User writer) {
    return Question.builder()
            .questionId(questionDTO.getQuestionId())
            .title(questionDTO.getTitle())
            .content(questionDTO.getContent())
            .writer(writer)
            .build();
  }

  // Entity → DTO 변환 메서드
  default QuestionDTO entityToDto(Question question, User writer, Long replyCount) {
    return QuestionDTO.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            .content(question.getContent())
            .writer(UserDTO.builder()  // UserDTO로 변환
                    .userId(writer.getUserId())
                    .name(writer.getName())
                    .build())
            .regDate(question.getRegDate())
            .modDate(question.getModDate())
            .replyCount(replyCount.intValue())
            .build();
  }

  // Question 등록
  Long register(QuestionDTO questionDTO);

  // 페이징된 Question 목록 조회
  PageResultDTO<QuestionDTO, Object[]> getList(PageRequestDTO pageRequestDTO);

  // 단일 Question 조회
  QuestionDTO get(Long questionId);

  // Question 수정
  void modify(QuestionDTO questionDTO);

  // Question 및 관련 댓글 삭제
  void removeWithAnswer(Long questionId);
}
