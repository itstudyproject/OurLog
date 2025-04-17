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

  private List<Object> followingUserList;

  // 팔로우 요청자 (팔로잉하는 사람)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_user_id")
  private User fromUser;

  // 팔로우 대상자 (팔로워 대상)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_user_id")
  private User toUser;

}
