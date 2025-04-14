package com.example.ourLog.entity;

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
  private User userId;
  private Long boardNo; // 1: 새소식, 2: 홍보게시판, 3: 요청게시판, 4: 자유게시판
  private String title;
  private String content;
  private String tag;
  private String fileName;

  private String replyCnt;

}
