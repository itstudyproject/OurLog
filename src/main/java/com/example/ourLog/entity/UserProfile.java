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
  private User nickname;

  private String introduction;
  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;
  private Long folowing;
  private Long folow;

  @OneToMany
  @JoinColumn(name = "pic_bought_list")
  private List<Trade> picBought;
  
  @OneToMany
  @JoinColumn(name = "pic_sold_list")
  private List<Trade> picSoldList;

  @OneToMany
  @JoinColumn(name = "is_bookmarked")
  private Bookmark isBookmarked;

  @OneToMany
  @JoinColumn(name = "bookmarked_post")
  private Bookmark bookmarkedPost;

  @OneToMany
  @JoinColumn(name = "bidding_list")
  private List<Trade> biddingList;

}
