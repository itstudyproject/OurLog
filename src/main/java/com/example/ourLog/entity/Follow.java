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
@ToString
@Table(name = "follow")

public class Follow extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long followId;

  private Long followCnt;
  private Long followingCnt;

  private List<User> followingUserList;

  @OneToOne(mappedBy = "follow", cascade = CascadeType.ALL)
  private UserProfile userProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Setter
  private Long toUser; // 팔로우 대상자 (팔로잉 당하는 사람)

  @Setter
  private Long fromUser; // 팔로우 요청자 (팔로잉하는 사람)

  public void setFollowCnt(Long followCnt) {
    this.followCnt = followCnt;
    if (this.userProfile != null) {
      this.userProfile.setFollowCnt(followCnt);
    }
  }
}
