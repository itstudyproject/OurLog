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

public class QnA extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long qnaId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  private User userId;
  private String title;
  private String content;
  private String replyCnt;

}
