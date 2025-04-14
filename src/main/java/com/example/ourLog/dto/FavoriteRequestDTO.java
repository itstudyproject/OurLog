package com.example.ourLog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequestDTO {

  private Long userId;    // 사용자 ID
  private Long postId;    // 게시물 ID
}