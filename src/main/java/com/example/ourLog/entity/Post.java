package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "post")
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  @JsonProperty
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id") // 🔥 추가된 부분
  @JsonProperty
  private UserProfile userProfile;

  private Long boardNo;
  private String title;

  @Lob
  private String content;

  private String tag;
  private String fileName;

  @Builder.Default
  private Long replyCnt = 0L;

  @Builder.Default
  @Column(nullable = false)
  private Long views = 0L;

  @Builder.Default
  @Column(nullable = false)
  private Long followers = 0L;

  @Builder.Default
  @Column(nullable = false)
  private Long downloads = 0L;

  @OneToOne(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Trade trade; // 이 필드명을 RankingService에서 post.getTrade()로 사용합니다.

  // 수정 메서드
  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeContent(String content) {
    this.content = content;
  }

  public void increaseViews() {
    this.views++;
  }

  public void increaseFollowers() {
    this.followers++;
  }

  public void increaseDownloads() {
    this.downloads++;
  }
}

