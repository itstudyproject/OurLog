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

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "following_user_list")
//  @JsonProperty
//  private List<User> followingUserList;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followed_user_id")
  @JsonProperty
  private User followedUserId;
}
