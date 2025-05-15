package com.example.ourLog.repository;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.ourLog.entity.QPost.post;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteInsertTest {

  @Autowired
  private FavoriteRepository favoriteRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PostRepository postRepository;

  @Test
  @Transactional
  @Commit
  void insertFavorites() {
    List<User> users = userRepository.findAll();
    List<Post> posts = postRepository.findAll();

    for (User user : users) {
      int numberOfFavorites = (int) (Math.random() * 5) + 1; // 1~5개 랜덤

      for (int i = 0; i < numberOfFavorites; i++) {
        Post randomPost = posts.get((int) (Math.random() * posts.size()));

        boolean randomFavorited = Math.random() < 0.7;

        if (favoriteRepository.existsByUserAndPost(user, randomPost)) {
          continue; // 중복 방지
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .post(randomPost)
                .favorited(randomFavorited)
                .build();

        favoriteRepository.save(favorite);
      }
    }
  }

  @Test
  @Transactional
  @Commit
  void updateFavoriteCntInFavorites() {
    List<Post> posts = postRepository.findAll();
    List<User> users = userRepository.findAll();

    for (Post post : posts) {
      Long count = favoriteRepository.countByPostAndFavoritedTrue(post);

      if (count == null) {
        count = 0L;
      }

      // 해당 post에 대한 모든 favorite 객체 가져오기
      for (User user : users) {
        // `findByUserAndPost`가 아닌 `findByPost`로 변경 (모든 즐겨찾기 리스트 찾기)
        List<Favorite> favorites = favoriteRepository.findByPost(post);

        for (Favorite favorite : favorites) {
          // 만약 favoriteCnt가 null이면 0으로 설정
          if (favorite.getFavoriteCnt() == null) {
            favorite.setFavoriteCnt(0L); // 기본값 설정
          }

          // favoriteCnt 업데이트
          favorite.setFavoriteCnt(count);
          favoriteRepository.save(favorite);
        }
      }
    }
  }

  @Test
  @Transactional
  void testFindByUserAndPost() {
    // 이미 DB에 저장되어 있는 User와 Post를 가져옴
    User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
    Post post = postRepository.findById(58L).orElseThrow(() -> new RuntimeException("Post not found"));

    Optional<Favorite> result = favoriteRepository.findByUserAndPost(user, post);
    System.out.println("Found Favorite: " + result); // 결과 출력
  }
}


