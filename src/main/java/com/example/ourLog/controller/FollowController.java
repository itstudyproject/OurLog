package com.example.ourLog.controller;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.FollowService;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/followers")
@RequiredArgsConstructor
@Log4j2
public class FollowController {

  private final FollowService followService;
  private final PostService postService;

  // ✅ 팔로우 요청
  @PostMapping("/{fromUserId}/follow/{toUserId}")
  public ResponseEntity<String> followUser(
      @AuthenticationPrincipal UserAuthDTO user,
      @PathVariable Long fromUserId,
      @PathVariable Long toUserId) {

    if (!user.getUserId().equals(fromUserId)) {
      log.warn("❌ 인증 사용자와 요청자가 다릅니다. 거부됨.");
      return ResponseEntity.status(403).body("권한 없음");
    }

    followService.follow(fromUserId, toUserId);
    log.info("✅ 팔로우 요청: fromUserId={}, toUserId={}", fromUserId, toUserId);
    return ResponseEntity.ok("{\"message\": \"팔로우 성공\"}"); // JSON 형식으로 반환
  }

  // ✅ 언팔로우 요청
  @DeleteMapping("/{fromUserId}/unfollow/{toUserId}")
  public ResponseEntity<String> unfollowUser(
      @AuthenticationPrincipal UserAuthDTO user,
      @PathVariable Long fromUserId,
      @PathVariable Long toUserId) {

    if (!user.getUserId().equals(fromUserId)) {
      log.warn("❌ 인증 사용자와 요청자가 다릅니다. 거부됨.");
      return ResponseEntity.status(403).body("권한 없음");
    }

    followService.unfollow(fromUserId, toUserId);
    log.info("✅ 언팔로우 요청: fromUserId={}, toUserId={}", fromUserId, toUserId);
    return ResponseEntity.ok("{\"message\": \"언팔로우 성공\"}"); // JSON 형식으로 반환
  }

  // ✅ 내가 팔로우하는 사용자 목록
  @GetMapping("/{fromUserId}/following")
  public ResponseEntity<List<UserDTO>> getFollowingList(
      @AuthenticationPrincipal UserAuthDTO user,
      @PathVariable Long fromUserId) {

    if (!user.getUserId().equals(fromUserId)) {
      return ResponseEntity.status(403).body(null);
    }

    List<UserDTO> followingList = followService.getFollowingList(fromUserId);
    return ResponseEntity.ok(followingList);
  }

  // ✅ 나를 팔로우하는 사용자 목록
  @GetMapping("/{fromUserId}/followers")
  public ResponseEntity<List<UserDTO>> getFollowersList(
      @AuthenticationPrincipal UserAuthDTO user,
      @PathVariable Long fromUserId) {

    if (!user.getUserId().equals(fromUserId)) {
      return ResponseEntity.status(403).body(null);
    }

    List<UserDTO> followerList = followService.getFollowerList(fromUserId);
    return ResponseEntity.ok(followerList);
  }

  //특정 사용자가 다른 사용자를 팔로우하는지 상태 확인
  @GetMapping("/status/isFollowing/{fromUserId}/{toUserId}")
  public ResponseEntity<Boolean> isFollowing(
      @AuthenticationPrincipal UserAuthDTO user,
      @PathVariable Long fromUserId,
      @PathVariable Long toUserId) {

    // 요청하는 사용자가 fromUserId와 일치하는지 확인 (인증 필요)
    if (user == null || !user.getUserId().equals(fromUserId)) {
      log.warn("❌ 팔로우 상태 확인 실패: 인증 사용자와 요청자가 다릅니다. 거부됨.");
      // 인증되지 않았거나 권한이 없는 경우, 팔로우하지 않음으로 처리하거나 403 에러 반환
      // 여기서는 간단히 false를 반환하지만, 보안상 403 Forbidden을 반환하는 것이 더 안전할 수 있습니다.
      // 현재 프론트엔드 로직은 403 시도 false로 처리하고 있으니, 일단 false 반환으로 맞춥니다.
      return ResponseEntity.status(HttpStatus.OK).body(false); // 또는 ResponseEntity.status(403).body(false);
    }

    log.info("✅ 팔로우 상태 확인 요청: fromUserId={}, toUserId={}", fromUserId, toUserId);
    // followService의 followUser 메서드는 fromUserId가 toUserId를 팔로우하는지 boolean 반환
    boolean isFollowing = followService.followUser(fromUserId, toUserId);

    return ResponseEntity.ok(isFollowing); // 팔로우 상태(true/false) 반환
  }

  @GetMapping("/getPost/{userId}")
  public ResponseEntity<List<PostDTO>> getPostById(@PathVariable Long userId) {
    List<PostDTO> postList = postService.getPostByUserId(userId);
    return ResponseEntity.ok(postList);
  }
}