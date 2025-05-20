package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.dto.FollowCountDTO;

import java.util.List;

public interface FollowService {

  default Follow dtoToEntity(FollowDTO followDTO) {
    return Follow.builder()
        .followCnt(followDTO.getFollowCnt())
        .followingCnt(followDTO.getFollowingCnt())
        .followId(followDTO.getFollowId())
        .build();
  }

  void follow(Long fromUserId, Long toUserId);

  void unfollow(Long fromUserId, Long toUserId);

  List<UserDTO> getFollowingList(Long userId);

  List<UserDTO> getFollowerList(Long userId);

  FollowCountDTO getFollowCount(Long userId); // ✅ 수정된 부분
}
