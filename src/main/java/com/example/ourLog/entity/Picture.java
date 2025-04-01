package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "picture")

public class Picture extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String name;
  private Long price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_nickname")
  private User userNickname;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private User userId;

  private String describe;
  private Long bookmark;
  private Long views;
  private Long downloads;
  private String tag;
  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;
}
