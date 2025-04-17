package com.example.ourLog.service;


import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.FavoriteRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  @Override
  @Transactional
  public FavoriteDTO toggleFavorite(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndPostId(user, post);

    if (favoriteOpt.isPresent()) {
      // 좋아요 취소
      favoriteRepository.delete(favoriteOpt.get());
      return entityToDTO(favoriteOpt.get());  // 엔티티를 DTO로 변환하여 반환
    } else {
      // 좋아요 추가
      Favorite favorite = Favorite.builder()
          .userId(user)    // User 객체 설정
          .postId(post)    // Post 객체 설정
          .favorited(true)  // true로 설정하여 좋아요 상태 추가
          .build();  // 빌더를 사용하여 객체 생성

      Favorite saved = favoriteRepository.save(favorite);
      return entityToDTO(saved);  // 저장된 엔티티를 DTO로 변환하여 반환
    }
  }

  @Override
  public boolean isFavorited(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.existsByUserIdAndPostId(user, post);
  }

  @Override
  public Long getFavoriteCount(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.countByPostIdAndFavoritedTrue(post);
  }
}
