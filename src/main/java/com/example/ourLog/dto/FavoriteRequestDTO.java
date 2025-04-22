package com.example.ourLog.dto;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Data
public class FavoriteRequestDTO {

  private UserDTO userDTO;    // 사용자 ID
  private PostDTO postDTO;    // 게시물 ID
}