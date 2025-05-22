package com.example.ourLog.dto;

import com.example.ourLog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PictureDTO {
  private Long picId;
  private String uuid;
  private String picName;
  private String path;

  private Long downloads;
  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;

  private UserDTO userDTO;
//  private String userNickname;
  private PostDTO postDTO;
}
