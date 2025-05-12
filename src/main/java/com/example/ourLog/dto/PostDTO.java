package com.example.ourLog.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
  private Long postId;
  private String title;
  private String content;
  private String category;
  private Long views;

  private String tag;
  private List<String> tags;
  private String fileName;
  private Long boardNo;

  private UserDTO userDTO;

  @Builder.Default
  private List<PictureDTO> pictureDTOList = new ArrayList<>();

  private List<String> imageFileNames;

  private Long replyCnt;

  private LocalDateTime regDate;
  private LocalDateTime modDate;

}

