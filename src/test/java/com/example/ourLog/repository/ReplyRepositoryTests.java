package com.example.ourLog.repository;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Reply;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReplyRepositoryTests {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReplyRepository replyRepository;

  @Test
  @Transactional
  @Commit  // 테스트 후에도 DB에 남기려면
  public void insertJournalComments() {
    IntStream.rangeClosed(1, 200).forEach(i -> {
      Long userId = (long) (Math.random() * 100) + 1;
      Long postId = (long) (Math.random() * 100) + 1;

      // 1) DB에 있는 Post/User 엔티티 참조 얻기
      Post post = postRepository.getReferenceById(postId);
      User user = userRepository.getReferenceById(userId);

      // 2) Reply 생성
      Reply reply = Reply.builder()
              .post(post)
              .user(user)
              .content("댓글..." + i)
              .build();

      // 3) 저장
      replyRepository.save(reply);
    });
  }
}