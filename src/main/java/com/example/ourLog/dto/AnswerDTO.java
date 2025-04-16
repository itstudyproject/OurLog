package com.example.ourLog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnswerDTO {
  private Long answerId;
  private String contents;

  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
