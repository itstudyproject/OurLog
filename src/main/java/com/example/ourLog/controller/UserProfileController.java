package com.example.ourLog.controller;

import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.service.FavoriteService;
import com.example.ourLog.service.TradeService;
import com.example.ourLog.service.UserProfileService;
import com.example.ourLog.service.UserService;
import com.example.ourLog.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
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
  private final FavoriteService favoriteService;
  private final FileUploadUtil fileUploadUtil;

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

  // 프로필 이미지 업로드
  @PostMapping("/upload-image/{userId}")
  public ResponseEntity<Map<String, String>> uploadProfileImage(
          @PathVariable Long userId,
          @RequestParam("file") MultipartFile file
  ) {
    try {
      // 프로필 이미지 업로드
      String imagePath = fileUploadUtil.uploadProfileImage(file, userId);

      // 프로필 정보 업데이트
      User user = userService.findByUserId(userId);

      UserProfileDTO profileDTO = new UserProfileDTO();
      profileDTO.setOriginImagePath(imagePath);

      // 프로필 업데이트
      UserProfileDTO updatedProfile = userProfileService.updateProfile(user, profileDTO);

      // 응답
      Map<String, String> response = new HashMap<>();
      response.put("imagePath", imagePath);

      return ResponseEntity.ok(response);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다."));
    }
  }

  // 프로필 부분 수정
  @PatchMapping("/profileEdit/{userId}")
  public ResponseEntity<UserProfileDTO> partialUpdateProfile(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> updates
  ) {

    User user = userService.findByUserId(userId);

    UserDTO userDTO = userService.entityToDTO(user);

    UserProfileDTO userProfileDTO = userProfileService.getProfileById(userId);

    // 닉네임 수정
    if (updates.containsKey("nickname")) {
      userDTO.setNickname((String) updates.get("nickname"));
    }

    // 이메일 수정
    if (updates.containsKey("email")) {
      userDTO.setEmail((String) updates.get("email"));
    }

    // 자기소개 수정
    if (updates.containsKey("introduction")) {
      userProfileDTO.setIntroduction((String) updates.get("introduction"));
    }

    User updatedUser = userService.dtoToEntity(userDTO);

    // 프로필 업데이트
    UserProfileDTO updatedProfile = userProfileService.updateProfile(updatedUser, userProfileDTO);

    return ResponseEntity.ok(updatedProfile);
  }

  // 좋아요 목록 조회
  @GetMapping("/favorites/{userId}")
  public ResponseEntity<List<FavoriteDTO>> getFavorites(@PathVariable Long userId) {
    log.info("get favorites for userId: {}", userId);
    List<FavoriteDTO> favorites = favoriteService.getFavoritesByUser(userId);
    return ResponseEntity.ok(favorites);
  }

  @PatchMapping("/accountEdit/{userId}")
  public ResponseEntity<UserDTO> userInfoEdit(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> updates
  ) {
    User user = userService.findByUserId(userId);

    UserDTO updatedUser = userService.entityToDTO(user);

    // 비밀번호 수정
    if (updates.containsKey("password")) {
      updatedUser.setPassword((String) updates.get("password"));
    }

    // 전화번호 수정
    if (updates.containsKey("mobile")) {
      updatedUser.setMobile((String) updates.get("mobile"));
    }

    userService.updateUser(updatedUser);
    return ResponseEntity.ok(updatedUser);
  }
}