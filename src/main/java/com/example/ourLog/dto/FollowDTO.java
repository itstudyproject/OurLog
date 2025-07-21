package com.example.ourLog.dto;

import com.example.ourLog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowDTO {
  private Long followId;
  private Long followCnt;
  private Long followingCnt;
  private UserDTO toUser;
  private UserDTO fromUser;
}
