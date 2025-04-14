package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
<<<<<<< Updated upstream
@Table(name = "post")
=======
@Table(name = "qna")
>>>>>>> Stashed changes

public class QnA extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long qnaId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id")
  private User userId;
<<<<<<< Updated upstream
  private String title;
  private String content;
  private String replyCnt;
=======

  private String title;
  private String content;

  private Long replyCnt;
>>>>>>> Stashed changes


}

