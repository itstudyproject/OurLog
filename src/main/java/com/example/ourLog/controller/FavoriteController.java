package com.example.ourLog.controller;

import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.dto.FavoriteRequestDTO;
import com.example.ourLog.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  // 좋아요 추가/취소 (토글)
  @PostMapping("/toggle")
  public ResponseEntity<FavoriteDTO> toggleFavorite(@RequestBody FavoriteRequestDTO request) {
    // request에서 userId와 postId를 정상적으로 가져올 수 있습니다.
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
}
