package com.example.ourLog.dto;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequestDTO {

  private User userId;    // 사용자 ID
  private Post postId;    // 게시물 ID
}