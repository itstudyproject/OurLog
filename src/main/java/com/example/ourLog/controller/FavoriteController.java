package com.example.ourLog.controller;

import com.example.ourLog.service.FavoriteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping
  public ResponseEntity<?> addFavorite(@RequestBody FavoriteRequest request) {
    favoriteService.addFavorite(request.getUserId(), request.getImageId());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{userId}/{imageId}")
  public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable Long imageId) {
    favoriteService.removeFavorite(userId, imageId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{userId}")
  public ResponseEntity<List<Image>> getFavorites(@PathVariable Long userId) {
    return ResponseEntity.ok(favoriteService.getFavoritesByUser(userId));
  }

  @GetMapping("/{userId}/{imageId}")
  public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long imageId) {
    return ResponseEntity.ok(favoriteService.isFavorite(userId, imageId));
  }
}