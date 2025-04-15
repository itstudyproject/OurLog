package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PictureDTO {
  private String uuid;
  private String picName;
  private String path;

  private String describe;
  private Long views;
  private Long downloads;
  private String tag;
  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;

  private Long userId;
  private String userNickname;
  private Long postId;
}
