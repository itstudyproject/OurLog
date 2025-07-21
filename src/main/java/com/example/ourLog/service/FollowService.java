package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.dto.FollowCountDTO;

import java.util.List;

public interface FollowService {

  // DTO → Entity 변환
  default Follow dtoToEntity(FollowDTO followDTO) {
    return Follow.builder()
            .followId(followDTO.getFollowId())
            .followCnt(followDTO.getFollowCnt())
            .followingCnt(followDTO.getFollowingCnt())
            .build();
  }

  // ✅ Entity → DTO 변환
  default FollowDTO entityToDTO(Follow follow) {
    return FollowDTO.builder()
            .followId(follow.getFollowId())
            .followCnt(follow.getFollowCnt())
            .followingCnt(follow.getFollowingCnt())
            .build();
  }

  // 팔로우 수행
  void follow(Long fromUserId, Long toUserId);

  // 언팔로우 수행
  void unfollow(Long fromUserId, Long toUserId);

  // 내가 팔로우하는 사람 목록
  List<UserDTO> getFollowingList(Long userId);

  // 나를 팔로우하는 사람 목록
  List<UserDTO> getFollowerList(Long userId);

  // ✅ 팔로우/팔로워 수 반환
  FollowCountDTO getFollowCount(Long userId);

  boolean followUser(Long fromUserId, Long toUserId);
}
