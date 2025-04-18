package com.example.ourLog.service;

import com.example.ourLog.dto.FavoriteDTO;
import java.util.List;
import java.util.stream.Collectors;
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
  public FavoriteDTO toggleFavorite(User userId, Post postId) {
    User user = userRepository.findById(userId.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId.getPostId())
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndPostId(userId, postId);

    if (favoriteOpt.isPresent()) {
      // 좋아요 취소
      favoriteRepository.deleteByUserIdAndPostId(userId, postId);  // 수정된 메서드 호출
      return entityToDTO(favoriteOpt.get());  // 엔티티를 DTO로 변환하여 반환
    } else {
      // 좋아요 추가
      Favorite favorite = Favorite.builder()
          .user(user)    // User 객체 설정
          .post(post)    // Post 객체 설정
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

  @Override
  @Transactional(readOnly = true)
  public List<FavoriteDTO> getFavoritesByUser(User user) {
    List<Favorite> favoriteList = favoriteRepository.findByUserId(user);

    return favoriteList.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }
}
