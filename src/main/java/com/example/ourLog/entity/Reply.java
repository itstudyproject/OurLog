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
  private String writer;


  @ManyToOne (fetch = FetchType.LAZY)
  Post post;

  public void changeText(String text) {
    this.text = text;
  }
}
