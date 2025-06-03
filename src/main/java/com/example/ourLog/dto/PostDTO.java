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

  private UserDTO userDTO;

  private Long postId;
  private String title;
  private String content;
  private Long favoriteCnt;

  @Column(nullable = false)
  private Long views = 0L;

  private Long followers;
  private Long downloads;

  private String tag;
  private String fileName;
  private String uuid;
  private String path;
  private Long boardNo;

  private TradeDTO tradeDTO;
  private Long userId;
  private String nickname;
  private String thumbnailImagePath;
  private List<String> originImagePath;
  private String resizedImagePath;
  private String profileImage;
  private UserProfileDTO userProfile;

  @Builder.Default
  private List<PictureDTO> pictureDTOList = new ArrayList<>();

  private List<ReplyDTO> replyDTOList;

  private Long replyCnt;

  private LocalDateTime regDate;
  private LocalDateTime modDate;

}
