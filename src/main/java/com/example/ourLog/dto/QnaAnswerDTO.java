package com.example.ourLog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QnaAnswerDTO {
  private Long answerId;
  private String contents;

  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
