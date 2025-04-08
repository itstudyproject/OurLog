package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "post")
public class Reply extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long replyId;
  private String text;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  private User userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_nickname")
  private User nickname;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_email")
  private User email;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post postId;

  public void changeText(String text) {
    this.text = text;
  }
}
