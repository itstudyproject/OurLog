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
@Table(name = "answer")
public class Answer extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long answerId;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_writer")
  @JsonProperty
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", unique = true)  // Question 게시글을 참조
  @JsonProperty
  private Question question;

  @Lob
  @JsonProperty
  private String contents;

  public Answer(User user, Question question, String contents) {
    this.user = user;
    this.question = question;
    this.contents = contents;
  }

  public void updateContents(String contents) {
    this.contents = contents;
  }

  public boolean isSameWriter(User loginUser) {
    return loginUser.equals(this.user);
  }
}
