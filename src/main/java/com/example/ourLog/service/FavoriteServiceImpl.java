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

    FavoriteDTO resultDto;

    if (favoriteOpt.isPresent()) {
      favoriteRepository.deleteByUserAndPost(user, post);

      // ✅ Post 엔티티의 좋아요 수 감소 및 저장
      post.decreaseFavoriteCnt();
      postRepository.save(post); // Post 엔티티 저장

      resultDto = FavoriteDTO.builder()
          .userId(userId)
          .postId(postId)
          .favorited(false) // 좋아요 취소됨
          .favoriteCnt(post.getFavoriteCnt()) // 감소 후의 최신 좋아요 수
          .build();
    } else {
      Favorite favorite = Favorite.builder()
          .user(user)
          .post(post)
          .favorited(true)
          .build();

      Favorite saved = favoriteRepository.save(favorite); // Favorite 레코드 저장

      // ✅ Post 엔티티의 좋아요 수 증가 및 저장
      post.increaseFavoriteCnt();
      postRepository.save(post); // Post 엔티티 저장

      resultDto = FavoriteDTO.builder()
          .favoriteId(saved.getFavoriteId()) // 새로 생성된 ID
          .userId(userId)
          .postId(postId)
          .favorited(true) // 좋아요 추가됨
          .favoriteCnt(post.getFavoriteCnt()) // 증가 후의 최신 좋아요 수
          .regDate(saved.getRegDate()) // BaseEntity 상속 시
          .modDate(saved.getModDate()) // BaseEntity 상속 시
          .build();
    }
    return resultDto;
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
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return post.getFavoriteCnt() != null ? post.getFavoriteCnt() : 0L;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FavoriteDTO> getFavoritesByUser(Long userId) {
    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<Favorite> favoriteList = favoriteRepository.findByUser(user);

    return favoriteList.stream()
        .map(favorite -> {
          FavoriteDTO dto = entityToDTO(favorite);
        dto.setFavoriteCnt(favorite.getPost() != null ? favorite.getPost().getFavoriteCnt() : 0L); // Post 엔티티에서 favoriteCnt 가져옴
          return dto;
        })
        .collect(Collectors.toList());
  }

}