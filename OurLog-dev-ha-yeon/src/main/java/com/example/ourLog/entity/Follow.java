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
@Table(name = "follow")

public class Follow extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long followId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_user_list")
  private List<User> followingUserList;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followed_user_id")
  private User followedUserId;
}
