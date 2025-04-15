package com.example.ourLog.service;

import com.example.ourLog.dto.FollowDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;

public interface FollowService {
  void followUser(User userId);

  default Follow dtoToEntity(FollowDTO followDTO) {
    Follow follow = Follow.builder()
            .followCnt(followDTO.get)
            .build();
  }
}
