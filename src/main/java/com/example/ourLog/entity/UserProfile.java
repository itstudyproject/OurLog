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
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User profile_id;

  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;
  private int folowing;
  private int folow;

  @OneToMany
  @JoinColumn(name = "pic_bought")
  private Picture picBought;
  @OneToMany
  @JoinColumn(name = "pic_marked")
  private Picture picMarked;
  @OneToMany
  @JoinColumn(name = "pic_sold")
  private Picture picSold;

}
