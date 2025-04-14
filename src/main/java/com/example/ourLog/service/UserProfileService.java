package com.example.ourLog.service;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.UserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface UserProfileService {

  default UserProfileDTO entityToDTO(UserProfile userProfile) {
    UserProfileDTO userProfileDTO = UserProfileDTO.builder()
            .boughtList(userProfile.getBoughtList())
            .soldList(userProfile.getSoldList())
            .isFavorited(userProfile.getIsFavorited())
            .favoritedPost(userProfile.getFavoritedPost())
            .build();
    return userProfileDTO;
  }

  default Map<String, Object> dtoToEnitity(UserProfileDTO userProfileDTO) {
    Map<String, Object> entityMap = new HashMap<>();

    UserProfile userProfile = UserProfile.builder()
            .userId(userProfileDTO.getUserId())
            .profileId(userProfileDTO.getProfileId())
            .nickname(userProfileDTO.getNickname())
            .introduction(userProfileDTO.getIntroduction())
            .originImagePath(userProfileDTO.getOriginImagePath())
            .thumbnailImagePath(userProfileDTO.getThumbnailImagePath())
            .followingCnt(userProfileDTO.getFollowingCnt())
            .followCnt(userProfileDTO.getFollowCnt())
            .isFavorited(userProfileDTO.getIsFavorited())
            .favoritedPost(userProfileDTO.getFavoritedPost())
            .build();
    entityMap.put("userProfile", userProfile);

    List<TradeDTO> tradeDTOList = userProfileDTO.getTradeList();
    if (tradeDTOList != null && tradeDTOList.size() > 0) {
      List<Trade> tradeList = tradeDTOList.stream().map(tradeDTO -> {
        Trade trade = Trade.builder()
                .tradeId(tradeDTO.getTradeId())
                .build();
        return trade;
      }).collect(Collectors.toList());
      entityMap.put("tradeList", tradeList);
    }
    return entityMap;
  }

  // 프로필 조회 (userId 기준)
  UserProfileDTO getProfile(Long userId);

  // 전체 프로필 목록
  List<UserProfileDTO> getAllProfiles();

  // 프로필 수정
  UserProfileDTO updateProfile(Long userId, UserProfileDTO profileDTO);

  // 프로필 삭제
  void deleteProfile(Long userId);
}
