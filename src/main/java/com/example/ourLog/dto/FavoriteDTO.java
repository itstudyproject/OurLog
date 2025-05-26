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

  private boolean favorited;
  private LocalDateTime regDate;
  private LocalDateTime modDate;


}

