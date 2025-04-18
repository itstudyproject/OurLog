package com.example.ourLog.service;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.FollowRepository;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;



  @Override
  public void follow(Long fromUserId, Long toUserId) {
    User fromUser = userRepository.findById(fromUserId)
        .orElseThrow(() -> new IllegalArgumentException("팔로우 요청자 없음"));
    User toUser = userRepository.findById(toUserId)
        .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 없음"));

    if (followRepository.existsByFromUserAndToUser(fromUser, toUser)) {
      return; // 중복 팔로우 방지
    }

    Follow follow = Follow.builder()
        .followCnt(1L)
        .followingCnt(1L)
        .toUser(toUser)
        .fromUser(fromUser)// 추가 필요: follow 요청자와의 연관관계
        .build();

    followRepository.save(follow);
  }

  @Override
  public void unfollow(Long fromUserId, Long toUserId) {
    User fromUser = userRepository.findById(fromUserId)
        .orElseThrow(() -> new IllegalArgumentException("언팔로우 요청자 없음"));
    User toUser = userRepository.findById(toUserId)
        .orElseThrow(() -> new IllegalArgumentException("언팔로우 대상 없음"));

    followRepository.deleteByFromUserAndToUser(fromUser, toUser);
  }

  @Override
  public List<UserDTO> getFollowingList(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    return followRepository.findAllByFromUser(user).stream()
        .map(f -> toUserDTO(f.getFromUser()))
        .collect(Collectors.toList());
  }

  @Override
  public List<UserDTO> getFollowerList(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    return followRepository.findAllByToUser(user).stream()
        .map(f -> toUserDTO(f.getToUser())) // 추가: Follow에 팔로우 요청자(User user) 필드 필요
        .collect(Collectors.toList());
  }

  private UserDTO toUserDTO(User user) {
    return UserDTO.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .build();
  }
}
