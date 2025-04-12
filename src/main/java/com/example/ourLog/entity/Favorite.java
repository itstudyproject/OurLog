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
  private User userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post postId;


  private boolean favorited;
  private int favoriteCnt;
  // 생성자 또는 빌더로만 값 설정. 별도 필드 필요 없음.
}
