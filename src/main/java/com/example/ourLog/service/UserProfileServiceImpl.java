package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
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

  @Override
  public UserProfileDTO createProfile(UserProfileDTO dto) {
    log.info("Creating profile for userId: " + dto.getUserId());


    User user = userRepository.findById(dto.getUserId().getUserId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

    UserProfile profile = UserProfile.builder()
        .profileId(user)
        .nickname(user)
        .introduction(dto.getIntroduction())
        .originImagePath(dto.getOriginImagePath())
        .thumbnailImagePath(dto.getThumbnailImagePath())
        .followingCnt(Follow.builder().followingCnt(dto.getFollowingCnt().getFollowingCnt()).build())
        .followCnt(Follow.builder().followCnt(dto.getFollowCnt().getFollowCnt()).build())
        .build();

    return toDTO(userProfileRepository.save(profile));
  }

  @Override
  public UserProfileDTO getProfile(Long userId) {
    UserProfile profile = userProfileRepository.findByProfileId_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

    return toDTO(profile);
  }

  @Override
  public List<UserProfileDTO> getAllProfiles() {
    return userProfileRepository.findAll().stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public UserProfileDTO updateProfile(Long userId, UserProfileDTO dto) {
    UserProfile profile = userProfileRepository.findByProfileId_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

//    profile.setIntroduction(dto.getIntroduction());
//    profile.setOriginImagePath(dto.getOriginImagePath());
//    profile.setThumbnailImagePath(dto.getThumbnailImagePath());
//    profile.setFollowCnt(dto.getFollowCnt());
//    profile.setFollowingCnt(dto.getFollowingCnt());
    profile.getIntroduction();
    profile.getOriginImagePath();
    profile.getThumbnailImagePath();
    profile.getFollowingCnt();
    profile.getFollowingCnt();

    return toDTO(userProfileRepository.save(profile));
  }

  @Override
  public void deleteProfile(Long userId) {
    UserProfile profile = userProfileRepository.findByProfileId_Id(userId)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

    userProfileRepository.delete(profile);
  }

  // ============ 🔁 Mapper ============

  private UserProfileDTO toDTO(UserProfile profile) {
    return UserProfileDTO.builder()
        .userId(profile.getProfileId())
        .nickname(profile.getNickname().getNickname())
        .introduction(profile.getIntroduction())
        .originImagePath(profile.getOriginImagePath())
        .thumbnailImagePath(profile.getThumbnailImagePath())
        .followCnt(profile.getFollowCnt())
        .followingCnt(profile.getFollowingCnt())
        .build();
  }
}