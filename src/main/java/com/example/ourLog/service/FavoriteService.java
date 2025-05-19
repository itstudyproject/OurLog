package com.example.ourLog.service;

import java.util.List;
import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;


public interface FavoriteService {

  // DTO -> Entity 변환

  default Favorite dtoToEntity(FavoriteDTO favoriteDTO, User user, Post post) {
    return Favorite.builder()
        .favoriteId(favoriteDTO.getFavoriteId())
        .user(user)
        .post(post)
        .favorited(favoriteDTO.isFavorited())
        .favoriteCnt(favoriteDTO.getFavoriteCnt())
        .build();
  }

  // Entity -> DTO 변환

  default FavoriteDTO entityToDTO(Favorite favorite) {
    return FavoriteDTO.builder()
        .favoriteId(favorite.getFavoriteId())
        .favoriteCnt(favorite.getFavoriteCnt())
        .userId(favorite.getUser().getUserId())  // assuming userId is needed
        .postDTO(PostDTO.builder()
                .postId(favorite.getPost().getPostId())
                .title(favorite.getPost().getTitle())
                .content(favorite.getPost().getContent())
                .userDTO(UserDTO.builder().userId(favorite.getUser().getUserId()).build())
                .build())  // assuming postId is needed
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

  // 사용자 기준으로 즐겨찾기 목록 조회
  List<FavoriteDTO> getFavoritesByUser(Long userId); // 변경된 파라미터 타입
}