package com.example.ourLog.dto;

import com.example.ourLog.entity.UserProfile;
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
  private Long views;

  private Long followers;
  private Long downloads;

  private String tag;
  private String fileName;
  private Long boardNo;

  private UserDTO userDTO;
  private UserProfileDTO userProfileDTO;

  @Builder.Default
  private List<PictureDTO> pictureDTOList = new ArrayList<>();

  private Long replyCnt;

  private LocalDateTime regDate;
  private LocalDateTime modDate;
  private String thumbPath;


  public PostDTOBuilder thumbPath(String thumbPath) {
    this.thumbPath = thumbPath;
    return this;
  }

}
