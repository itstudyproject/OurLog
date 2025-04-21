package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = {"post", "user"})
public class Reply extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long replyId;

  @Lob
  private String content;

//  private Long replyCnt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  @JsonProperty
  private User user;

  private String nickname;

  private String email;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  @JsonProperty
  private Post post;

  public void changeContent(String content) {
    this.content = content;
  }
}