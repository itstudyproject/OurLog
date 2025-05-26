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
  public FavoriteDTO toggleFavorite(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndPost(user, post);

    if (favoriteOpt.isPresent()) {
      favoriteRepository.deleteByUserAndPost(user, post);
      Long updatedCount = favoriteRepository.countByPost_PostIdAndFavoritedTrue(postId); // ✅ 수정

      FavoriteDTO dto = entityToDTO(favoriteOpt.get());
      dto.setFavoriteCnt(updatedCount);
      dto.setFavorited(false);
      return dto;
    } else {
      Favorite favorite = Favorite.builder()
          .user(user)
          .post(post)
          .favorited(true)
          .build();

      Favorite saved = favoriteRepository.save(favorite);
      Long updatedCount = favoriteRepository.countByPost_PostIdAndFavoritedTrue(postId); // ✅ 수정

      FavoriteDTO dto = entityToDTO(saved);
      dto.setFavoriteCnt(updatedCount);
      return dto;
    }
  }


  @Override
  public boolean isFavorited(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.existsByUserAndPost(user, post);
  }

  @Override
  public Long getFavoriteCount(Long postId) {
    return favoriteRepository.countByPost_PostIdAndFavoritedTrue(postId); // ✅ 수정 완료
  }

  @Override
  @Transactional(readOnly = true)
  public List<FavoriteDTO> getFavoritesByUser(Long userId) {
    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<Favorite> favoriteList = favoriteRepository.findByUser(user);

    return favoriteList.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }

}