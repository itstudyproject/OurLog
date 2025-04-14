package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

<<<<<<< Updated upstream
=======
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;

>>>>>>> Stashed changes
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {
  private Long replyId;
<<<<<<< Updated upstream
  private Long postId;
  private Long userId;
  private String nickname;
  private String email;
  private Long likes;
=======
  private Post postId;
  private User userId;
  private User nickname;
  private User email;
>>>>>>> Stashed changes
  private String text;
  private LocalDateTime regDate, modDate;
}
