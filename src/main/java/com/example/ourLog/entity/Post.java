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

  private Long boardNo; // 1: 새소식, 2: 홍보, 3: 요청, 4: 자유
  private String title;

  @Lob
  private String content;

  private String tag;
  private String fileName;

  @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
  private Long replyCnt;

  @Column(nullable = false)
  private Long views = 0L; // 조회수 (기본값 0)

  // 수정 메서드
  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeContent(String content) {
    this.content = content;
  }

  // 조회수 증가 메서드
  public void increaseViews() {
    this.views++;
  }
}
