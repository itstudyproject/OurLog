package com.example.ourLog.controller;

import com.example.ourLog.service.FollowService;
import com.example.ourLog.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/followers")
@RequiredArgsConstructor
@Log4j2

public class FollowController {

  private final FollowService followService;

  // 팔로우 요청
  @PostMapping("/{fromUserId}/follow/{toUserId}")
  public ResponseEntity<String> followUser(
      @PathVariable Long fromUserId,
      @PathVariable Long toUserId) {
    followService.follow(fromUserId, toUserId);
    return ResponseEntity.ok("팔로우 완료");
  }

  // 언팔로우 요청
  @DeleteMapping("/{fromUserId}/unfollow/{toUserId}")
  public ResponseEntity<String> unfollowUser(
      @PathVariable Long fromUserId,
      @PathVariable Long toUserId) {
    followService.unfollow(fromUserId, toUserId);
    return ResponseEntity.ok("언팔로우 완료");
  }

  // 특정 사용자의 팔로잉 목록 조회
  @GetMapping("/{userId}/following")
  public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long userId) {
    List<UserDTO> followingList = followService.getFollowingList(userId);
    return ResponseEntity.ok(followingList);
  }

  // 특정 사용자의 팔로워 목록 조회
  @GetMapping("/{userId}/followers")
  public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long userId) {
    List<UserDTO> followersList = followService.getFollowerList(userId);
    return ResponseEntity.ok(followersList);
  }
}
