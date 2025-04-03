package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "reply")

public class Reply {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long replyId;
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "")
  private User writer;
}
