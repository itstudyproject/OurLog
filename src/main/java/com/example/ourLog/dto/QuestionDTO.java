package com.example.ourLog.dto;

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
  private UserDTO writer;
  private LocalDateTime regDate, modDate;

  @JsonInclude (Include.NON_NULL)
  private Long answerId;
  private String answerContent;
}