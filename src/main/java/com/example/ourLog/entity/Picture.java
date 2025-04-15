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
@Table(name = "picture")

public class Picture extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long picId; // 그림 번호

  private String uuid;

  private String picName;

  private String path;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_nickname")
  @JsonProperty
  private User userNickname;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  @JsonProperty
  private User userId;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  @JsonProperty
  private Post postId;

  private String describe;
  private Long views;
  private Long downloads;
  private String tag;
  private String originImagePath;
  private String thumbnailImagePath;
  private String resizedImagePath;

}

