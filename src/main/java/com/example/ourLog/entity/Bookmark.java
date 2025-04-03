package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "bookmark")

public class Bookmark extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long bookmarkId;


  @OneToMany
  @JoinColumn(name = "prod_marked")
  private Long prodMarked;

}
