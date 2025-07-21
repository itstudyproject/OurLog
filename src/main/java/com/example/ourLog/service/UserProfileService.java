package com.example.ourLog.service;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.Follow;
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
  UserProfileDTO getProfileById(Long userId);

  // 전체 프로필 목록
  List<UserProfileDTO> getAllProfiles();

  // 프로필 수정
  UserProfileDTO updateProfile(User user, UserProfileDTO profileDTO);

  // 프로필 삭제
  void deleteProfile(User user);

  // DTO → Entity
  default UserProfile dtoToEntity(UserProfileDTO dto) {
    return UserProfile.builder()
            .user(User.builder()
                    .userId(dto.getUserId())
                    .email(dto.getEmail())
                    .nickname(dto.getNickname())
                    .name(dto.getName())
                    .mobile(dto.getMobile())
                    .build())
            .introduction(dto.getIntroduction())
            .originImagePath(dto.getOriginImagePath())
            .thumbnailImagePath(dto.getThumbnailImagePath())
            .build();
  }

  // Entity → DTO
  default UserProfileDTO entityToDto(UserProfile profile) {
    return UserProfileDTO.builder()
            .profileId(profile.getProfileId())                                // profile PK
            .userId(profile.getUser().getUserId()) // 유저 FK
            .nickname(profile.getUser().getNickname())
            .mobile(profile.getUser().getMobile())
            .introduction(profile.getIntroduction())
            .originImagePath(profile.getOriginImagePath())
            .thumbnailImagePath(profile.getThumbnailImagePath())
            .email(profile.getUser().getEmail())
            .name(profile.getUser().getName())
            .followCnt(profile.getFollowCnt())
            .followingCnt(profile.getFollowingCnt())
            .isFollowing(false) // ✅ 추가!
            .build();
  }

}
