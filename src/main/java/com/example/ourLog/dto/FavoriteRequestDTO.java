package com.example.ourLog.dto;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Data
public class FavoriteRequestDTO {

  private Long userId;    // 사용자 ID
  private Long postId;    // 게시물 ID
}