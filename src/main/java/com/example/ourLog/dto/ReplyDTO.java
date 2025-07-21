package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {
  private Long replyId;
  private Long postId;
  private PostDTO postDTO;
  private UserDTO userDTO;
  private String nickname;
  private String email;
  private String content;
  private LocalDateTime regDate, modDate;
}