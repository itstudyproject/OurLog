package com.example.ourLog.service;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.FavoriteRepository;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final PictureRepository pictureRepository;

  public void addFavorite(Long userId, Long postId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found"));

    // 이제 엔티티로 존재 여부 확인 가능
    if (favoriteRepository.existsByUserIdAndPostId(user, post)) {
      throw new IllegalStateException("이미 즐겨찾기 되어있어요!");
    }



    Favorite favorite = Favorite.builder()
        .userId(user)
        .postId
        .isFavorited(true) // 필요에 따라 true/false 설정
        .build();

    favoriteRepository.save(favorite);
  }

  public List<Picture> getFavoritesByUser(Long userId) {
    return favoriteRepository.findByUserId(userId).stream()
        .map(Favorite::getPicture)
        .collect(Collectors.toList());
  }
}