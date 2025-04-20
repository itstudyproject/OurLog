package com.example.ourLog.dto;

import com.example.ourLog.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
  private Long questionId;
  private String title;
  private String content;
  private UserDTO userDTO;
  private LocalDateTime regDate, modDate;

  @JsonInclude (Include.NON_NULL)
  private AnswerDTO answerDTO;
//  private String answerContent;
}