package com.example.ourLog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"favorites", "user"})

public class FavoriteDTO {
  private Long favoriteId;
  private Long userId;
  private Long postId;
  private boolean favorited;
  private LocalDateTime regDate;
  private LocalDateTime modDate;

  private Long favoriteCnt;
}

