package com.example.ourLog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

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
  @Setter
  @Getter
  private boolean favorited;
  private LocalDateTime regDate;
  private LocalDateTime modDate;

  // 👉 아래 getter/setter 꼭 있어야 함!
  @Setter
  @Getter
  private Long favoriteCnt;

  // userId, postId 등도 마찬가지
}

