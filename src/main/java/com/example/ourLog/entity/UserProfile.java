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
<<<<<<< Updated upstream
  private User profileId;
=======
  private User profileId; // 
>>>>>>> Stashed changes

  @OneToOne
  @JoinColumn(name = "user_nickname")
  private User nickname; // 닉네임

  private String introduction; // 자기소개
<<<<<<< Updated upstream
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
  @JoinColumn(name = "is_bookmarked")
  private Bookmark isBookmarked; // 북마크

  @OneToMany
  @JoinColumn(name = "bookmarked_post")
  private Bookmark bookmarkedPost; // 북마크한 게시물
=======
  private String originImagePath; // 프사 원본
  private String thumbnailImagePath; // 프사 썸네일
//  private String resizedImagePath;
  private Long folowing; // 팔로잉
  private Long folow; // 팔로우

  @OneToMany
  @JoinColumn(name = "pic_bought")
  private Trade picBought; // 구매한 그림
  
  @OneToMany
  @JoinColumn(name = "pic_sold")
  private Trade picSold; // 판매한 그림

  @OneToMany
  @JoinColumn(name = "is_bookmarked")
  private Bookmark isBookmarked; // 북마크 했는지 x

  @OneToMany
  @JoinColumn(name = "bookmarked_post")
  private Bookmark bookmarkedPost; // 북마크게시물목록

  @OneToMany
  @JoinColumn(name = "is_bidding_id")
  private Trade isBiddingId; // <입찰현황> 구매예정인 작품(입찰 진행중)
>>>>>>> Stashed changes





}
