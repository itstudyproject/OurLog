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


    Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndPost(user, post);


    if (favoriteOpt.isPresent()) {
      // ✅ 좋아요 취소 + 최신 좋아요 수 반영
      favoriteRepository.deleteByUserAndPost(user, post);
      Long updatedCount = favoriteRepository.countByPostAndFavoritedTrue(post);

      FavoriteDTO dto = entityToDTO(favoriteOpt.get());
      dto.setFavoriteCnt(updatedCount);  // 좋아요 수 반영
      dto.setFavorited(false);           // 상태도 false로
      return dto;
    } else {
      // ✅ 좋아요 추가
      Favorite favorite = Favorite.builder()
          .user(user)
          .post(post)
          .favorited(true)
          .build();

      Favorite saved = favoriteRepository.save(favorite);
      Long updatedCount = favoriteRepository.countByPostAndFavoritedTrue(post);

      FavoriteDTO dto = entityToDTO(saved);
      dto.setFavoriteCnt(updatedCount);  // 추가 후 좋아요 수 반영
      return dto;
    }
  }

  @Override
  public boolean isFavorited(User userId, Post postId) {
    User user = userRepository.findById(userId.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId.getPostId())
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.existsByUserAndPost(user, post);
  }

  @Override
  public Long getFavoriteCount(Post postId) {
    Post post = postRepository.findById(postId.getPostId())
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.countByPostAndFavoritedTrue(post);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FavoriteDTO> getFavoritesByUser(User user) {
    List<Favorite> favoriteList = favoriteRepository.findByUser(user);

    return favoriteList.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }

}