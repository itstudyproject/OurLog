package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.repository.FollowRepository;
import com.example.ourLog.repository.UserProfileRepository;
import com.example.ourLog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FollowRepository followRepository; // ✅ 추가

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

    return entityToDto(userProfileRepository.save(profile));
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