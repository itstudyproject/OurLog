package com.example.ourLog.dto;

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

  // 속성을 사용하거나 선언 시점에 또는 생성자에서 기본값으로 초기화
  @Builder.Default  //@AllArgsConstructor없으면 에러발생
  private List<PictureDTO> pictureDTOList = new ArrayList<>();

  private UserDTO userDTO;
  private Long replyCnt = 0L;

  private String tag;
  private String fileName;
  private Long boardNo;

  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
