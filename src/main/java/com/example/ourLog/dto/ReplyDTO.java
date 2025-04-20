package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {
  private Long replyId;
  private Post post;
  private User user;
  private String nickname;
  private String email;
  private String content;
  private LocalDateTime regDate, modDate;
}