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
public class FavoriteDTO {
  private Long favoriteId;
  private Long userId;
  private Long postId;
  private boolean favorited;
  private LocalDateTime regDate;
  private LocalDateTime modDate;

}

