package com.example.ourLog.controller;

import com.example.ourLog.service.FavoriteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor

public class FavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping("/api/favorites")
  @ResponseStatus(HttpStatus.OK)
  public void create(
      @Valid @RequestBody FavoriteRequest favoriteRequest) {
    favoriteService.create(favoriteRequest);
  }

  @PutMapping("/api/favorites/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void update(@PathVariable Long id,
                     @Valid @RequestBody FavoriteRequest favoriteRequest,
                     @AuthenticationPrincipal JwtAuthentication auth) {
    favoriteService.update(auth.email, id, favoriteRequest);
  }

  @DeleteMapping("/api/favorites/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void delete(@PathVariable Long id,
                     @AuthenticationPrincipal JwtAuthentication auth) {
    favoriteService.delete(auth.email, id);
  }


}
