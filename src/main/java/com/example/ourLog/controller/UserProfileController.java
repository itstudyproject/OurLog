package com.example.ourLog.controller;

import com.example.ourLog.dto.*;
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
    // TODO: 생성 시 User가 존재하는지 서비스 레이어에서 확인하거나 여기서 확인 후 예외 처리 필요
    UserProfileDTO created = userProfileService.createProfile(profileDTO);
    return ResponseEntity.ok(created);
  }

  // ✅ 유저 ID로 프로필 조회
  @GetMapping("/get/{userId}")
  public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
    log.info("get profile for userId: {}", userId);
    try {
      UserProfileDTO profile = userProfileService.getProfileById(userId);
      return ResponseEntity.ok(profile);
    } catch (IllegalArgumentException e) {
      log.warn("Profile not found for userId: {}", userId, e);
      return ResponseEntity.notFound().build();
    }
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
          @PathVariable Long userId, // User 객체 대신 userId를 받도록 수정
          @RequestBody UserProfileDTO profileDTO
  ) {
    log.info("update profile for userId: {}", userId);
    try {
      // 서비스 레이어에서 User와 UserProfile을 모두 찾도록 하거나
      // 여기서 User를 먼저 찾고 예외 처리 후 서비스 호출
      User user = userService.findByUserId(userId); // User 조회 시도

      // UserProfileService의 updateProfile 메소드가 userId를 받도록 수정 필요
      // 또는 updateProfile 서비스 메소드가 User 객체를 받도록 유지하고 여기서 조회한 user 전달
      // 현재 서비스 메소드는 User user 객체를 받으므로, 여기서 조회한 user를 전달합니다.
      UserProfileDTO updated = userProfileService.updateProfile(user, profileDTO);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      // userService.findByUserId 또는 userProfileService.updateProfile에서 예외 발생 시
      log.warn("User or Profile not found for userId: {}", userId, e);
      return ResponseEntity.notFound().build();
    }
  }

  // ✅ 프로필 삭제
  @DeleteMapping("/delete/{userId}")
  public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) { // User 객체 대신 userId를 받도록 수정
    log.info("delete profile for userId: {}", userId);
    try {
      User user = userService.findByUserId(userId); // User 조회 시도
      userProfileService.deleteProfile(user); // UserProfileService의 deleteProfile은 User 객체를 받음
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      // userService.findByUserId 또는 userProfileService.deleteProfile에서 예외 발생 시
      log.warn("User or Profile not found for userId: {}", userId, e);
      return ResponseEntity.notFound().build();
    }
  }

  // 구매목록
  @GetMapping("/purchases/{userId}")
  public ResponseEntity<Map<String, List<TradeDTO>>> getPurchaseList(@PathVariable Long userId) {
    log.info("get purchase list for userId: {}", userId);
    // tradeService.getPurchaseList(userId)에서 User를 찾지 못했을 때 예외 처리가 필요하다면 동일하게 try-catch 적용
    Map<String, List<TradeDTO>> purchases = tradeService.getPurchaseList(userId);
    return ResponseEntity.ok(purchases);
  }

  // 판매 목록 조회
  @GetMapping("/sales/{userId}")
  public ResponseEntity<List<TradeDTO>> getSalesList(@PathVariable Long userId) {
    log.info("get sales list for userId: {}", userId);
    // tradeService.getSalesList(userId)에서 User를 찾지 못했을 때 예외 처리가 필요하다면 동일하게 try-catch 적용
    List<TradeDTO> salesList = tradeService.getSalesList(userId);
    return ResponseEntity.ok(salesList);
  }

  // 프로필 이미지 업로드
  @PostMapping("/upload-image/{userId}")
  public UploadResultDTO uploadProfileImage(MultipartFile file, Long userId) throws IOException {
    // FileUploadUtil의 uploadFile 메서드로 파일 저장 및 썸네일 생성
    // 이 메소드는 UploadResultDTO를 반환하며 예외 발생 시 호출자(여기서는 컨트롤러)에게 IOException을 던집니다.
    // 여기서 예외를 잡고 ResponseEntity로 감싸 반환하는 것이 좋습니다.
    try {
        // userId를 사용하여 User를 찾고 유효성 검사를 수행할 수 있습니다.
        // User user = userService.findByUserId(userId); // User 유효성 검사가 필요하다면 추가

        // FileUploadUtil의 uploadProfileImage 메서드로 변경 (userId를 인자로 받음)
        UploadResultDTO result = fileUploadUtil.uploadProfileImage(file, userId);
        // 성공 응답
        // TODO: UploadResultDTO를 ResponseEntity로 감싸 반환하도록 수정 필요
        // return ResponseEntity.ok(result);
        return result; // 현재 UploadResultDTO 그대로 반환하는 구조 유지
    } catch (IllegalArgumentException e) {
         // userId가 유효하지 않은 경우 등 userService.findByUserId에서 발생 가능
         log.warn("User not found for userId: {}", userId, e);
         // TODO: ResponseEntity로 감싸 반환하도록 수정 필요 (예: return ResponseEntity.notFound().build();)
         throw e; // 현재 구조상 그대로 예외를 다시 던집니다.
    }
    catch (IOException e) {
        log.error("Failed to upload profile image for userId: {}", userId, e);
        // 파일 업로드/처리 중 IO 예외 발생 시 500 Internal Server Error 반환
        // TODO: ResponseEntity로 감싸 반환하도록 수정 필요 (예: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();)
         throw e; // 현재 구조상 그대로 예외를 다시 던집니다.
    }
  }

  // 썸네일 경로 반환이 필요하다면 아래처럼 추가 메서드도 가능
  // 이 메소드는 컨트롤러의 엔드포인트가 아니므로 직접적인 HTTP 응답 반환은 필요 없습니다.
  public String getThumbnailPath(String imagePath) {
    // imagePath: profile/yyyy/MM/dd/uuid_filename.jpg
    // 썸네일:    profile/yyyy/MM/dd/s_uuid_filename.jpg
    int lastSlash = imagePath.lastIndexOf("/");
    String dir = imagePath.substring(0, lastSlash + 1);
    String file = imagePath.substring(lastSlash + 1);
    return dir + "s_" + file;
  }

  // 프로필 부분 수정
  @PatchMapping("/profileEdit/{userId}")
  public ResponseEntity<UserProfileDTO> partialUpdateProfile(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> updates
  ) {
    log.info("partial update profile for userId: {}", userId);
    try {
      // User와 UserProfile 조회 시도
      User user = userService.findByUserId(userId);
      // UserProfileDTO userProfileDTO = userProfileService.getProfileById(userId); // partialUpdateProfile 서비스에서 조회하도록 변경 제안

      // 기존 로직을 서비스 레이어로 옮기거나, 여기서 조회 후 업데이트 로직 수행
      // 현재 서비스 메소드 시그니처에 맞춰 User 객체와 업데이트 내용을 전달하는 방식 유지
      // partialUpdateProfile 서비스 메소드가 User와 Map<String, Object>을 받도록 수정했다면
      // UserProfileDTO updatedProfile = userProfileService.partialUpdateProfile(user, updates);
      // 여기서는 기존 updateProfile 메소드에 DTO를 넘겨주는 구조로 수정 (updates 맵을 DTO로 변환하거나 서비스에서 처리)
      // 현재 UserProfileServiceImpl에는 partialUpdateProfile 메소드가 없으므로 updateProfile을 사용한다고 가정
      // updateProfile은 User와 UserProfileDTO를 받으므로 updates 맵을 UserProfileDTO로 변환해야 합니다.
      // 혹은 UserProfileService에 updates 맵을 직접 처리하는 partialUpdateProfile 메소드를 추가하는 것이 좋습니다.

      // 임시 방편으로 기존 로직 유지 (서비스 호출 전 여기서 조회 및 업데이트)
      UserDTO userDTO = userService.entityToDTO(user);
      UserProfileDTO userProfileDTO = userProfileService.getProfileById(userId); // 여기서 조회 가능

      // 닉네임, 이메일, 자기소개, 이미지 경로 업데이트 로직 (기존 코드에서 복사)
      if (updates.containsKey("nickname")) {
        userDTO.setNickname((String) updates.get("nickname"));
      }
      if (updates.containsKey("email")) {
        userDTO.setEmail((String) updates.get("email"));
      }
      if (updates.containsKey("introduction")) {
        userProfileDTO.setIntroduction((String) updates.get("introduction"));
      }
      if (updates.containsKey("originImagePath")) {
        userProfileDTO.setOriginImagePath((String) updates.get("originImagePath"));
      }
      if (updates.containsKey("thumbnailImagePath")) {
        userProfileDTO.setThumbnailImagePath((String) updates.get("thumbnailImagePath"));
      }

      // 업데이트된 정보를 서비스로 전달하여 최종 저장
      User updatedUser = userService.dtoToEntity(userDTO);
      UserProfileDTO updatedProfile = userProfileService.updateProfile(updatedUser, userProfileDTO); // updateProfile 호출

      return ResponseEntity.ok(updatedProfile);

    } catch (IllegalArgumentException e) {
      // userService.findByUserId 또는 userProfileService.getProfileById, userProfileService.updateProfile에서 예외 발생 시
      log.warn("User or Profile not found for userId: {}", userId, e);
      return ResponseEntity.notFound().build();
    }
     catch (Exception e) {
        log.error("An error occurred during partial update for userId: {}", userId, e);
        // 기타 다른 예외 발생 시 500 Internal Server Error 반환
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // 좋아요 목록 조회
  @GetMapping("/favorites/{userId}")
  public ResponseEntity<List<PostDTO>> getFavorites(@PathVariable Long userId) {
    log.info("get favorites for userId: {}", userId);
     try {
         // favoriteService.getFavoritesByUser(userId) 내부에서 User를 찾지 못하면 예외 발생 가능성 있음
         List<PostDTO> favorites = favoriteService.getFavoritesByUser(userId);
         return ResponseEntity.ok(favorites);
     } catch (IllegalArgumentException e) {
         log.warn("User not found for userId: {}", userId, e);
         return ResponseEntity.notFound().build();
     }
  }

  @PatchMapping("/accountEdit/{userId}")
  public ResponseEntity<UserDTO> userInfoEdit(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> updates
  ) {
    log.info("account edit for userId: {}", userId);
    try {
        // User 조회 시도
        User user = userService.findByUserId(userId);

        UserDTO updatedUser = userService.entityToDTO(user);

        // 비밀번호 수정
        if (updates.containsKey("password")) {
          // TODO: 비밀번호는 암호화하여 저장해야 합니다. 평문으로 설정하면 안 됩니다.
          updatedUser.setPassword((String) updates.get("password"));
        }

        // 전화번호 수정
        if (updates.containsKey("mobile")) {
          updatedUser.setMobile((String) updates.get("mobile"));
        }

        // userService.updateUser(updatedUser) 호출
        // userService.updateUser 내부에서 예외 발생 가능성도 고려해야 합니다.
        userService.updateUser(updatedUser);
        return ResponseEntity.ok(updatedUser);
    } catch (IllegalArgumentException e) {
        log.warn("User not found for userId: {}", userId, e);
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        log.error("An error occurred during account edit for userId: {}", userId, e);
        // 기타 다른 예외 발생 시 500 Internal Server Error 반환
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}