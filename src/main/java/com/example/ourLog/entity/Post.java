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
  private User userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_nickname")
  @JsonProperty
  private User nickname;



  private Long boardNo; // 1: 새소식, 2: 홍보게시판, 3: 요청게시판, 4: 자유게시판
  private String title;

  @Lob
  private String content;
  
  private String tag;
  private String fileName;

  private Long replyCnt;

  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeContent(String content) {
    this.content = content;
  }
}