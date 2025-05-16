package com.example.ourLog.controller;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.TradeService;
import com.example.ourLog.service.UserProfileService;
import com.example.ourLog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserProfileController {
  private final UserService userService;
  private final UserProfileService userProfileService;
  private final TradeService tradeService;

  // ✅ 프로필 생성
  @PostMapping("/create")
  public ResponseEntity<UserProfileDTO> createProfile(@RequestBody UserProfileDTO profileDTO) {
    log.info("create profile for userId: {}", profileDTO.getUserId());
    UserProfileDTO created = userProfileService.createProfile(profileDTO);
    return ResponseEntity.ok(created);
  }

  // ✅ 유저 ID로 프로필 조회
  @GetMapping("/get/{userId}")
  public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
    log.info("get profile for userId: {}", userId);

    UserProfileDTO profile = userProfileService.getProfileById(userId);
    return ResponseEntity.ok(profile);
  }


  // ✅ 모든 프로필 조회
  @GetMapping("/profiles")
  public ResponseEntity<List<UserProfileDTO>> getAllProfiles() {
    log.info("get all profiles");
    List<UserProfileDTO> profiles = userProfileService.getAllProfiles();
    return ResponseEntity.ok(profiles);
  }

  // ✅ 프로필 수정
  @PutMapping("/edit/{userId}")
  public ResponseEntity<UserProfileDTO> updateProfile(
          @PathVariable User user,
          @RequestBody UserProfileDTO profileDTO
  ) {
    log.info("update profile for userId: {}", user);
    UserProfileDTO updated = userProfileService.updateProfile(user, profileDTO);
    return ResponseEntity.ok(updated);
  }

  // ✅ 프로필 삭제
  @DeleteMapping("/delete/{userId}")
  public ResponseEntity<Void> deleteProfile(@PathVariable User user) {
    log.info("delete profile for userId: {}", user);
    userProfileService.deleteProfile(user);
    return ResponseEntity.noContent().build();
  }

  // 구매목록
  @GetMapping("/purchases/{userId}")
  public ResponseEntity<Map<String, List<TradeDTO>>> getPurchaseList(@PathVariable Long userId) {
    log.info("get purchase list for userId: {}", userId);
    Map<String, List<TradeDTO>> purchases = tradeService.getPurchaseList(userId);
    return ResponseEntity.ok(purchases);
  }

  // 판매 목록 조회
  @GetMapping("/sales/{userId}")
  public ResponseEntity<List<TradeDTO>> getSalesList(@PathVariable Long userId) {
    log.info("get sales list for userId: {}", userId);
    List<TradeDTO> salesList = tradeService.getSalesList(userId);
    return ResponseEntity.ok(salesList);
  }
}