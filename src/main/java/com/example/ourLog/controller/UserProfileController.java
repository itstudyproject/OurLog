package com.example.ourLog.controller;

import com.example.ourLog.dto.FavoriteDTO;
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.dto.UploadResultDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.service.FavoriteService;
import com.example.ourLog.service.TradeService;
import com.example.ourLog.service.UserProfileService;
import com.example.ourLog.service.UserService;
import com.example.ourLog.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  public UploadResultDTO uploadProfileImage(MultipartFile file, Long userId) throws IOException {
    // FileUploadUtil의 uploadFile 메서드로 파일 저장 및 썸네일 생성
    return fileUploadUtil.uploadFile(file, "profile", 100, 100);
  }

  // 썸네일 경로 반환이 필요하다면 아래처럼 추가 메서드도 가능
  public String getThumbnailPath(String imagePath) {
    // imagePath: profile/yyyy/MM/dd/uuid_filename.jpg
    // 썸네일:    profile/yyyy/MM/dd/s_uuid_filename.jpg
    int lastSlash = imagePath.lastIndexOf("/");
    String dir = imagePath.substring(0, lastSlash + 1);
    String file = imagePath.substring(lastSlash + 1);
    return dir + "s_" + file;
  }
  // 프로필 부분 수정
// 백엔드 partialUpdateProfile 메서드 수정 제안
  @PatchMapping("/profileEdit/{userId}")
  public ResponseEntity<UserProfileDTO> partialUpdateProfile(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> updates
  ) {
    // User와 UserProfile 조회 (기존 로직)
    User user = userService.findByUserId(userId);
    UserDTO userDTO = userService.entityToDTO(user);
    UserProfileDTO userProfileDTO = userProfileService.getProfileById(userId);

    // 닉네임 수정 (기존 로직)
    if (updates.containsKey("nickname")) {
      userDTO.setNickname((String) updates.get("nickname"));
    }

    // 이메일 수정 (기존 로직)
    if (updates.containsKey("email")) {
      userDTO.setEmail((String) updates.get("email"));
    }

    // 자기소개 수정 (기존 로직)
    if (updates.containsKey("introduction")) {
      userProfileDTO.setIntroduction((String) updates.get("introduction"));
    }

    // *** 프로필 이미지 경로 수정 로직 추가 ***
    if (updates.containsKey("originImagePath")) {
      userProfileDTO.setOriginImagePath((String) updates.get("originImagePath"));
    }
    if (updates.containsKey("thumbnailImagePath")) {
      userProfileDTO.setThumbnailImagePath((String) updates.get("thumbnailImagePath"));
    }
    // ***************************************

    User updatedUser = userService.dtoToEntity(userDTO);

    // 서비스 레이어를 통한 프로필 업데이트
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