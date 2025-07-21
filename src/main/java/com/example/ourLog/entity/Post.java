package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
  private Long downloads = 0L;

  @Builder.Default
  @Column(nullable = false)
  private Long favoriteCnt = 0L;

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("regDate DESC") // 최신 Trade가 목록의 앞에 오도록 정렬 (선택 사항이지만 유용)
  @Builder.Default // Builder 패턴 사용 시 기본값 설정
  private List<Trade> trades = new ArrayList<>(); // Trade 목록을 저장할 필드

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Picture> pictureList = new ArrayList<>();

  @Version
  private Long version;

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

  public void increaseDownloads() {
    this.downloads++;
  }

  // ✅ 좋아요 수 증가/감소 메소드 추가
  public void increaseFavoriteCnt() {
    this.favoriteCnt = (this.favoriteCnt == null ? 0L : this.favoriteCnt) + 1L;
  }

  public void decreaseFavoriteCnt() {
    // 0 미만으로 내려가지 않도록 방지
    this.favoriteCnt = (this.favoriteCnt == null || this.favoriteCnt <= 0L) ? 0L : this.favoriteCnt - 1L;
  }

}

