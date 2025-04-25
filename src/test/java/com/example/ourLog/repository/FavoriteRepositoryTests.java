package com.example.ourLog.repository;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteRepositoryTests {

  @Autowired
  FavoriteRepository favoriteRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PostRepository postRepository;

  @Transactional
  @Test
  public void testSaveAndFindFavorite() {
    // User와 Post는 미리 DB에 있어야 하므로, 먼저 조회하거나 생성
    Optional<User> userOpt = userRepository.findByEmail("r1@r.r", false);
    Optional<Post> postOpt = postRepository.findById(1L); // 예: 1번 글이 존재한다고 가정

    assertTrue(userOpt.isPresent());
    assertTrue(postOpt.isPresent());

    User user = userOpt.get();
    Post post = postOpt.get();

    // Favorite 생성 및 저장
    Favorite favorite = Favorite.builder()
        .user(user)
        .post(post)
        .favorited(true)
        .build();

    favoriteRepository.save(favorite);

    // 저장된 Favorite 확인
    Optional<Favorite> saved = favoriteRepository.findByUserAndPost(user, post);
    assertTrue(saved.isPresent());
    assertEquals(user.getUserId(), saved.get().getUser().getUserId());
  }   // 사용자(User)가 특정 게시물(Post)를 즐겨찾기(Favorite)로 등록하고, 그게 잘 저장되고 조회되는지를 테스트

  @Transactional
  @Test
  public void testExistsByUserAndPost() {
    Optional<User> userOpt = userRepository.findByEmail("r1@r.r", false);
    Optional<Post> postOpt = postRepository.findById(1L);

    assertTrue(userOpt.isPresent());
    assertTrue(postOpt.isPresent());

    boolean exists = favoriteRepository.existsByUserAndPost(userOpt.get(), postOpt.get());
    System.out.println("Favorite exists: " + exists);
  }    //  주어진 유저와 게시물에 대해 해당하는 즐겨찾기 항목이 존재하는지 여부를 확인

  @Transactional
  @Test
  public void testDeleteFavorite() {
    Optional<User> userOpt = userRepository.findByEmail("r1@r.r", false);
    Optional<Post> postOpt = postRepository.findById(1L);

    assertTrue(userOpt.isPresent());
    assertTrue(postOpt.isPresent());

    favoriteRepository.deleteByUserAndPost(userOpt.get(), postOpt.get());

    Optional<Favorite> result = favoriteRepository.findByUserAndPost(userOpt.get(), postOpt.get());
    assertFalse(result.isPresent());
  }   // 특정 유저가 특정 게시물을 즐겨찾기에서 삭제할 수 있는지를 테스트하는 코드

  @Transactional
  @Test
  public void testCountFavorites() {
    Optional<Post> postOpt = postRepository.findById(1L);

    assertTrue(postOpt.isPresent());

    Long count = favoriteRepository.countByPostAndFavoritedTrue(postOpt.get());
    System.out.println("Favorited count for post 1: " + count);
  }  //  특정 게시물에 대해 '즐겨찾기'가 얼마나 달렸는지(즐겨찾기를 한 유저 수를) 확인하는 테스트
}
