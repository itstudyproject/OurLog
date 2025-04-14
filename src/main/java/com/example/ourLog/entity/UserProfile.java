package com.example.ourLog.entity;


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

  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;
  private Long folowing;
  private Long folow;

  @OneToMany
  @JoinColumn(name = "pic_bought")
  private Trade picBought;
  
  @OneToMany
  @JoinColumn(name = "pic_sold")
  private Trade picSold;

  @OneToMany
  @JoinColumn(name = "is_bookmarked")
  private Bookmark isBookmarked;

  @OneToMany
  @JoinColumn(name = "bookmarked_post")
  private Bookmark bookmarkedPost;

}
