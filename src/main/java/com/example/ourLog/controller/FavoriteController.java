package com.example.ourLog.controller;

import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.dto.FavoriteRequestDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.FavoriteService;
import com.example.ourLog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Log4j2

public class FavoriteController {

  private final FavoriteService favoriteService;
  private final UserService userService;

  // 좋아요 추가/취소 (토글)
  @PostMapping("/toggle")
  public ResponseEntity<FavoriteDTO> toggleFavorite(@RequestBody FavoriteRequestDTO request) {
    // request에서 user와 post를 정상적으로 가져올 수 있습니다.
    log.info("Toggle favorite 요청 - userId: {}, postId: {}", request.getUserId(), request.getPostId());

    FavoriteDTO result = favoriteService.toggleFavorite(request.getUserId(), request.getPostId());
    return ResponseEntity.ok(result);
  }

  // 해당 유저가 해당 게시글을 좋아요 했는지 여부 확인
  @GetMapping("/{userId}/{postId}")
  public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long postId) {
    return ResponseEntity.ok(favoriteService.isFavorited(userId, postId));
  }

  // 해당 게시글의 전체 좋아요 수 조회
  @GetMapping("/count/{postId}")
  public ResponseEntity<Long> getFavoriteCount(@PathVariable Long postId) {
    return ResponseEntity.ok(favoriteService.getFavoriteCount(postId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PostDTO>> getFavoritesByUserId(@PathVariable Long userId) {
    List<PostDTO> favorites = favoriteService.getFavoritesByUser(userId);
    return ResponseEntity.ok(favorites);
  }
}