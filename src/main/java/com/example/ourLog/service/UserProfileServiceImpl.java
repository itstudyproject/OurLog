package com.example.ourLog.service;

import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.repository.FollowRepository;
import com.example.ourLog.repository.UserProfileRepository;
import com.example.ourLog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FollowRepository followRepository; // ✅ 추가
  private final SendBirdApiService sendbirdApiService;

  @Override
  public UserProfileDTO createProfile(UserProfileDTO dto) {
    log.info("Creating profile for userId: " + dto.getUserId());

    User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

    UserProfile profile = UserProfile.builder()
            .profileId(dto.getProfileId())
            .user(user) // user 설정
            .followCnt(dto.getFollowCnt())
            .followingCnt(dto.getFollowingCnt())
            .introduction(dto.getIntroduction())
            .originImagePath(dto.getOriginImagePath())
            .thumbnailImagePath(dto.getThumbnailImagePath())
            .build();

    UserProfile savedProfile = userProfileRepository.save(profile);

     // ✅ Sendbird User 생성/업데이트 호출 (프로필 이미지/닉네임 동기화)
     // User 엔티티에 닉네임 정보가 있으므로 User 객체를 전달하여 동기화
     String requestId = MDC.get("requestId"); // MDC에서 requestId 가져오기
     log.info("[{}] Calling SendbirdApiService.createUser from UserProfileServiceImpl.createProfile for userId: {}", requestId, dto.getUserId());
     try {
         // sendbirdApiService.createUser가 이제 Mono 대신 Map을 반환
         Map<String, Object> sendbirdUser = sendbirdApiService.createUser(user, requestId); // 블록킹 호출
         log.info("[{}] Sendbird User created/updated via profile creation: {}", requestId, sendbirdUser.get("user_id"));
     } catch (Exception e) {
         log.error("[{}] Error creating/updating Sendbird user via profile creation for userId: {}", requestId, dto.getUserId(), e);
         // Sendbird 사용자 생성 실패가 프로필 생성 자체를 막을 필요는 없을 수 있으므로 로깅만 하고 진행
     }

    return entityToDto(savedProfile);
  }

  @Override
  public UserProfileDTO getProfileById(Long userId) {
    UserProfile profile = userProfileRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

    // 기본 DTO 변환
    UserProfileDTO dto = entityToDto(profile);

    // follow 수, following 수 조회
    long followCnt = followRepository.countByToUser(profile.getUser());
    long followingCnt = followRepository.countByFromUser(profile.getUser());

    // DTO에 설정
    dto.setFollowCnt(followCnt);
    dto.setFollowingCnt(followingCnt);

    return dto;
  }

  @Override
  public List<UserProfileDTO> getAllProfiles() {
    return userProfileRepository.findAll().stream()
            .map(this::entityToDto)
            .collect(Collectors.toList());
  }

  @Override
  public UserProfileDTO updateProfile(User user, UserProfileDTO dto) {
    // 사용자 프로필 조회
    UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

    // 사용자 정보 업데이트
    User existingUser = profile.getUser();

    // 닉네임 중복 체크 (필요한 경우)
    if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
      // 닉네임 중복 확인 로직 추가 (필요한 경우)
      existingUser.setNickname(dto.getNickname());
    }

    // 프로필 정보 업데이트
    if (dto.getIntroduction() != null) {
      profile.setIntroduction(dto.getIntroduction());
    }

    // 프로필 이미지 업데이트
    if (dto.getOriginImagePath() != null) {
      profile.setOriginImagePath(dto.getOriginImagePath());
    }
    if (dto.getThumbnailImagePath() != null) {
      profile.setThumbnailImagePath(dto.getThumbnailImagePath());
    }

    // 사용자와 프로필 저장
    userRepository.save(existingUser);
    UserProfile updatedProfile = userProfileRepository.save(profile);

     String requestId = MDC.get("requestId"); // MDC에서 requestId 가져오기
     log.info("[{}] Calling SendbirdApiService.updateUser from UserProfileServiceImpl.updateProfile for userId: {}", requestId, user.getUserId());
     try {
         // sendbirdApiService.updateUser가 이제 Mono 대신 Map을 반환
         Map<String, Object> sendbirdUser = sendbirdApiService.updateUser(user, requestId); // 블록킹 호출
         log.info("[{}] Sendbird User updated via profile update: {}", requestId, sendbirdUser.get("user_id"));
     } catch (Exception e) {
         log.error("[{}] Error updating Sendbird user via profile update for userId: {}", requestId, user.getUserId(), e);
         // Sendbird 사용자 업데이트 실패가 프로필 업데이트 자체를 막을 필요는 없을 수 있으므로 로깅만 하고 진행
     }

    return entityToDto(updatedProfile);
  }

  @Override
  public void deleteProfile(User user) {
    UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

    userProfileRepository.delete(profile);
  }

  // ============ 🔁 Mapper ============

//  private UserProfileDTO toDTO(UserProfile profile) {
//    return UserProfileDTO.builder()
//        .user(profile.getUser())
//        .introduction(profile.getIntroduction())
//        .originImagePath(profile.getOriginImagePath())
//        .thumbnailImagePath(profile.getThumbnailImagePath())
//        .build();
//  }
}