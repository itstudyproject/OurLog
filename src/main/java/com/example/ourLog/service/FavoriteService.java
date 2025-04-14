package com.example.ourLog.service;

import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;


public interface FavoriteService {

  // DTO -> Entity 변환

  default Favorite dtoToEntity(FavoriteDTO favoriteDTO, Long userId, Long postId) {
    User user = User.builder().userId(userId).build();
    Post post = Post.builder().postId(postId).build();

    return Favorite.builder()
        .favoriteId(favoriteDTO.getFavoriteId())
        .userId(user)
        .postId(post)
        .favorited(favoriteDTO.isFavorited())

        .build();
  }

  // Entity -> DTO 변환

  default FavoriteDTO entityToDTO(Favorite favorite) {
    return FavoriteDTO.builder()
        .favoriteId(favorite.getFavoriteId())
        .userId(favorite.getUserId().getUserId())  // assuming userId is needed
        .postId(favorite.getPostId().getPostId())  // assuming postId is needed
        .favorited(favorite.isFavorited())
        // assuming favoriteCnt is needed
        .regDate(favorite.getRegDate())
        .modDate(favorite.getModDate())
        .build();
  }


  // 좋아요 추가 및 취소 (토글)
  FavoriteDTO toggleFavorite(Long userId, Long postId);

  // 좋아요 여부 확인
  boolean isFavorited(Long userId, Long postId);

  // 게시글에 대한 좋아요 수 조회
  Long getFavoriteCount(Long postId);


}
