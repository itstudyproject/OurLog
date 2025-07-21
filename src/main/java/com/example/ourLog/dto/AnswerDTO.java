package com.example.ourLog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerDTO {
  private Long answerId;
  private String contents;
  private UserDTO userDTO;


  private LocalDateTime regDate;
  private LocalDateTime modDate;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private QuestionDTO questionDTO;
}
