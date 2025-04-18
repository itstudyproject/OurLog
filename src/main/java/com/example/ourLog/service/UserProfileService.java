package com.example.ourLog.service;

import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;

import java.util.List;

public interface UserProfileService {


//  UserProfileDTO get(Long userId);
//
//  void get(UserProfileDTO profileDTO);
//
//  List<UserProfileDTO> getAllProfiles(Long userId);


  // 프로필 생성
  UserProfileDTO createProfile(UserProfileDTO profileDTO);

  // 프로필 조회 (userId 기준)
  UserProfileDTO getProfile(Long userId);

  // 전체 프로필 목록
  List<UserProfileDTO> getAllProfiles();

  // 프로필 수정
  UserProfileDTO updateProfile(Long userId, UserProfileDTO profileDTO);

  // 프로필 삭제
  void deleteProfile(Long userId);

  // DTO → Entity
  default UserProfile dtoToEntity(User user, UserProfileDTO dto) {
    return UserProfile.builder()
            .user(user)
            .introduction(dto.getIntroduction())
            .originImagePath(dto.getOriginImagePath())
            .thumbnailImagePath(dto.getThumbnailImagePath())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .follow(dto.getFollow())
            .build();
  }

  // Entity → DTO
  default UserProfileDTO entityToDto(UserProfile profile) {
    return UserProfileDTO.builder()
            .user(profile.getUser())
            .nickname(profile.getUser().getNickname())
            .introduction(profile.getIntroduction())
            .originImagePath(profile.getOriginImagePath())
            .thumbnailImagePath(profile.getThumbnailImagePath())
            .follow(profile.getFollow())
            .build();
  }
}
