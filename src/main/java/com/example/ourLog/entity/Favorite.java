package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "post"})
public class Favorite extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long favoriteId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;


  private boolean favorited;

  @Builder.Default
  @Column(name = "favorite_cnt")
  private Long favoriteCnt = 0L;  // 기본값 0


  public void setFavoriteCnt(Long favoriteCnt) {
    this.favoriteCnt = favoriteCnt;
  }


}