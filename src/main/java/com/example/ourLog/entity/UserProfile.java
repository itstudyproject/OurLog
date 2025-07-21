package com.example.ourLog.entity;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "user_profile")
public class UserProfile extends BaseEntity {

  @Id
  private Long profileId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  private String introduction;
  private String originImagePath;
  private String thumbnailImagePath;

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "follow_id")
//  @JsonProperty
//  private Follow follow;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "tradeId")
  @JsonProperty
  private List<Trade> boughtList;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "tradeId")
  @JsonProperty
  private List<Trade> soldList;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "is_favorited")
  @JsonProperty
  private Favorite favorite;

  // 새로 추가된 필드
  private Long followCnt;
  private Long followingCnt;

  // ✅ 핵심: Entity → DTO 변환
//  public UserProfileDTO toDTOWithUser() {
//    return UserProfileDTO.builder()
//        .profileId(this.profileId)
//        .thumbnailImagePath(this.thumbnailImagePath)
//        .originImagePath(this.originImagePath)
//        .introduction(this.introduction)
//        .user(UserDTO.builder()
//            .nickname(this.user.getNickname())
//            .build())
//        .build();
//  }
}
