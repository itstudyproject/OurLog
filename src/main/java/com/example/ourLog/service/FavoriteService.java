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

  public void addFavorite(Long userId, Long picId) {
    if (favoriteRepository.existsByUserIdAndImageId(userId, picId)) {
      throw new IllegalStateException("이미 즐겨찾기 되어있어요!");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    Picture picture = pictureRepository.findById()
        .orElseThrow(() -> new EntityNotFoundException("Image not found"));

    Favorite favorite = new Favorite();
    favorite.setUser(userId);
    favorite.setPicture(picture);

    favoriteRepository.save(favorite);
  }

  public void removeFavorite(Long userId, Long imageId) {
    favoriteRepository.deleteByUserIdAndImageId(userId, imageId);
  }

  public List<Picture> getFavoritesByUser(Long userId) {
    return favoriteRepository.findByUserId(userId).stream()
        .map(Favorite::getPostId)
        .collect(Collectors.toList());
  }

  public boolean isFavorite(Long userId, Long imageId) {
    return favoriteRepository.existsByUserIdAndImageId(userId, imageId);
  }
}
