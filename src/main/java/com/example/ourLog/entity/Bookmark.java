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
<<<<<<< Updated upstream

  private Long bookmarkId;

=======
  private Long bookmarkId;
>>>>>>> Stashed changes

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post postId;

  private boolean isBookmarked;
}
