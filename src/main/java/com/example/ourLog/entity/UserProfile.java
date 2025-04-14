package com.example.ourLog.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "user_profile")

public class UserProfile extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User profileId;

  @OneToOne
  @JoinColumn(name = "user_nickname")
  private User nickname; // 닉네임

  private String introduction; // 자기소개
  private String originImagePath; // 프사원본
  private String thumbnailImagePath; // 썸네일
//  private String resizedImagePath;
  private Long followingCnt; // 팔로잉
  private Long followCnt; // 팔로우

  @OneToMany
  @JoinColumn(name = "bought_list")
  private List<Trade> boughtList; // 구매목록(+입찰현황)
  
  @OneToMany
  @JoinColumn(name = "sold_list")
  private List<Trade> soldList; // 판매목록(+판매현황)

  @OneToMany
  @JoinColumn(name = "is_favorited")

  private Favorite isFavorited;

  @OneToMany
  @JoinColumn(name = "favorited_post")
  private Favorite favoritedPost;



}
// dd