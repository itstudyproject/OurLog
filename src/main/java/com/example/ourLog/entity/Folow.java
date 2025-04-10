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
@Table(name = "folow")

public class Folow extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long folowId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "folowing_user_list")
  private List<User> folowingUserList;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "folowed_user_id")
  private User folowedUserId;
}
