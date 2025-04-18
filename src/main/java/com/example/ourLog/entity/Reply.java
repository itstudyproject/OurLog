package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"postId", "userId"})
public class Reply extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long replyId;

  @Lob
  private String content;

  private Long replyCnt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  @JsonProperty
  private User user;

  @Setter
  @Column(unique = true)
  @JoinColumn(name = "writer_nickname")
  private String nickname;

  @Setter
  @Column(unique = true)
  @JoinColumn(name = "writer_email")
  private String email;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  @JsonProperty
  private Post postId;

  public void changeContent(String content) {
    this.content = content;
  }
}