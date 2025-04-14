package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
<<<<<<< Updated upstream
@ToString(exclude = "post")
=======
@ToString(exclude = {"post", "user"})
>>>>>>> Stashed changes
public class Reply extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long replyId;
  private String text;
<<<<<<< Updated upstream
  private String writer;


  @ManyToOne (fetch = FetchType.LAZY)
  Post post;
=======

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
>>>>>>> Stashed changes

  public void changeText(String text) {
    this.text = text;
  }
}
