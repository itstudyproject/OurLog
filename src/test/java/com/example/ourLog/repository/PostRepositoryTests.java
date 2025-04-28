package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostRepositoryTests {

  @Autowired
  private PostRepository postRepository;
  @Autowired
  private PictureRepository pictureRepository;

  @Test
  @Transactional
  @Commit
  public void insertPosts() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      // 1~4 중 랜덤하게 boardNo
      long boardNo = (long) (Math.random() * 4) + 1;

      // 예시 태그 배열 (원하는 값으로 바꿔도 됩니다)
      String[] tags = {"spring", "java", "react", "mysql", "dev"};
      String tag = tags[i % tags.length];

      // 예시 파일명
      String fileName = "post_attach_" + i + ".jpg";

      Post post = Post.builder()
              .user(User.builder().userId((long) i).build())
              .boardNo(boardNo)
              .title("Title..." + i)
              .content("Content..." + i)
              .tag(tag)            // 여기서 태그 세팅
              .fileName(fileName)  // 여기서 파일명 세팅
              .build();
      postRepository.save(post);

      int count = (int) (Math.random() * 5) + 1;
      for (int j = 0; j < count; j++) {
        Picture picture = Picture.builder()
                .uuid(UUID.randomUUID().toString())
                .post(post)
                .picName("picture" + j + ".jpg")
                .build();
        pictureRepository.save(picture);
      }
    });
  }
}