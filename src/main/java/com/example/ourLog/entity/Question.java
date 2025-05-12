package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "question")

public class Question extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long questionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  @JsonProperty
  private User user;

  private String title;
  private String content;

  @Builder.Default
  @Column(nullable = false)
  private boolean isOpen = true;

  public void changeQuestionTitle(String title) {this.title = title;}
  public void changeQuestionContent(String content) {this.content = content;}

  @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Answer> answers;

  // Answer가 존재하는지 체크하는 메서드 추가
  public boolean isAnswered() {
    return this.answer != null;
  }

}
