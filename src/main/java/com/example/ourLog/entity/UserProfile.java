package com.example.ourLog.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

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
  @MapsId // 외래키를 PK로 사용할 때
  @JoinColumn(name = "profile_id")
  private User user;

  private String introduction; // 자기소개
  private String originImagePath; // 프사원본
  private String thumbnailImagePath; // 썸네일
  //  private String resizedImagePath;

  @OneToOne
  @JoinColumn(name = "follow_id")
  @JsonProperty
  private Follow follow; // 팔로잉

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "bought_list")
  @JsonProperty
  private List<Trade> boughtList; // 판매목록(+판매현황)

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "sold_list")
  @JsonProperty
  private List<Trade> soldList; // 판매목록(+판매현황)


  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "is_favorited")
  @JsonProperty
  private Favorite favorite;

}