package com.example.ourLog.entity;

import com.example.ourLog.security.dto.UserAuthDTO;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_writer")
  @JsonProperty
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", unique = true)
  @JsonProperty
  private Question question;

  @Getter
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

  // UserAuthDTO와 비교하도록 수정
  public boolean isSameWriter(UserAuthDTO userAuthDTO) {
    return this.user.getEmail().equals(userAuthDTO.getEmail());
  }
}
