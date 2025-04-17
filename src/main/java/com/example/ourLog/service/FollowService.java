package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;

import java.util.List;

public interface FollowService {


  default Follow dtoToEntity(FollowDTO followDTO) {
    Follow follow = Follow.builder()
            .followCnt(followDTO.getFollowCnt())
            .followingCnt(followDTO.getFollowingCnt())
            .followId(followDTO.getFollowId())
            .build();
    return follow;
  }

  void follow(Long fromUserId, Long toUserId);

  void unfollow(Long fromUserId, Long toUserId);

  List<UserDTO> getFollowingList(Long userId);

  List<UserDTO> getFollowerList(Long userId);
}
