package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class QnaAnswer extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_writer")
  @JsonProperty
  private User writer;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "qna_id", unique = true)  // QnA 게시글을 참조
  @JsonProperty
  private QnA question;

  @Lob
  @JsonProperty
  private String contents;

  public QnaAnswer(User writer, QnA question, String contents) {
    this.writer = writer;
    this.question = question;
    this.contents = contents;
  }

  public void updateContents(String contents) {
    this.contents = contents;
  }

  public boolean isSameWriter(User loginUser) {
    return loginUser.equals(this.writer);
  }
}
