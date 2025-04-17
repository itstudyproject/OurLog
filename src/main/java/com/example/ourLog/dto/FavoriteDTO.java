package com.example.ourLog.dto;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
  private Long favoriteId;
  private User userId;
  private Post postId;
  private boolean favorited;
  private LocalDateTime regDate;
  private LocalDateTime modDate;

}

