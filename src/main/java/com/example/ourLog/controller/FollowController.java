package com.example.ourLog.controller;

import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.FollowService;
import com.example.ourLog.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@Log4j2
public class FollowController {

  private final FollowService followService;

  // ✅ 팔로우 요청
  @PostMapping("/{toUserId}")
  public ResponseEntity<String> followUser(
          @AuthenticationPrincipal UserAuthDTO user,
          @PathVariable Long toUserId) {

    Long fromUserId = user.getUserId();
    followService.follow(fromUserId, toUserId);
    return ResponseEntity.ok("팔로우 완료");
  }

  // ✅ 언팔로우 요청
  @DeleteMapping("/{toUserId}")
  public ResponseEntity<String> unfollowUser(
          @AuthenticationPrincipal UserAuthDTO user,
          @PathVariable Long toUserId) {

    Long fromUserId = user.getUserId();
    followService.unfollow(fromUserId, toUserId);
    return ResponseEntity.ok("언팔로우 완료");
  }

  // ✅ 내가 팔로우하는 사용자 목록
  @GetMapping("/following")
  public ResponseEntity<List<UserDTO>> getFollowingList(
          @AuthenticationPrincipal UserAuthDTO user) {

    List<UserDTO> followingList = followService.getFollowingList(user.getUserId());
    return ResponseEntity.ok(followingList);
  }

  // ✅ 나를 팔로우하는 사용자 목록
  @GetMapping("/followers")
  public ResponseEntity<List<UserDTO>> getFollowersList(
          @AuthenticationPrincipal UserAuthDTO user) {

    List<UserDTO> followerList = followService.getFollowerList(user.getUserId());
    return ResponseEntity.ok(followerList);
  }
}
