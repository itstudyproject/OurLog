package com.example.ourLog.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"}) // ✅ 중복 방지 (유저 + 게시글 조합)
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

  // ✅ 유저 삭제 시 favorite도 함께 삭제되도록 cascade 설정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // ✅ 게시글 삭제 시 favorite도 함께 삭제되도록 cascade 설정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  private boolean favorited;

  @Builder.Default
  private Long favoriteCnt = 0L;

  // ============ Setter / Helper Methods =============

  public void setUser(User user) {
    this.user = user;
    // ✅ 연관 객체와 데이터 동기화 예시 (있다면)
    if (!user.getFavorites().contains(this)) {
      user.getFavorites().add(this); // 양방향이라면
    }
  }

  public void setPost(Post post) {
    this.post = post;
    if (!post.getFavorites().contains(this)) {
      post.getFavorites().add(this); // 양방향이라면
    }
  }

  public void increaseFavoriteCnt() {
    this.favoriteCnt++;
  }

  public void decreaseFavoriteCnt() {
    if (this.favoriteCnt > 0) {
      this.favoriteCnt--;
    }
  }
}
