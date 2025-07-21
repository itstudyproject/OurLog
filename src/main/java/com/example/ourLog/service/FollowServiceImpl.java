package com.example.ourLog.service;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.FollowRepository;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.example.ourLog.dto.FollowCountDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            .fromUser(fromUser)
            .toUser(toUser)
            .build();

    followRepository.save(follow);
  }

  @Transactional
  @Override
  public void unfollow(Long fromUserId, Long toUserId) {
    User fromUser = userRepository.findById(fromUserId)
            .orElseThrow(() -> new IllegalArgumentException("언팔로우 요청자 없음"));
    User toUser = userRepository.findById(toUserId)
            .orElseThrow(() -> new IllegalArgumentException("언팔로우 대상 없음"));

    followRepository.deleteByFromUserAndToUser(fromUser, toUser);
  }

  // ✅ 내가 팔로우하는 유저 목록 (toUser)
  @Override
  public List<UserDTO> getFollowingList(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    return followRepository.findAllByFromUser(user).stream()
            .map(f -> toUserDTO(f.getToUser()))  // ✅ toUser로 수정
            .collect(Collectors.toList());
  }

  // ✅ 나를 팔로우하는 유저 목록 (fromUser)
  @Override
  public List<UserDTO> getFollowerList(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    return followRepository.findAllByToUser(user).stream()
            .map(f -> toUserDTO(f.getFromUser()))  // ✅ fromUser로 수정
            .collect(Collectors.toList());
  }

  // User → UserDTO 변환
  private UserDTO toUserDTO(User user) {
    return UserDTO.builder()
            .userId(user.getUserId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .build();
  }

  @Override
  public FollowCountDTO getFollowCount(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    Long followerCount = followRepository.countByToUser(user);
    Long followingCount = followRepository.countByFromUser(user);

    return new FollowCountDTO(followerCount, followingCount);
  }

  @Override
  public boolean followUser(Long fromUserId, Long toUserId) {
    User fromUser = userRepository.findById(fromUserId)
        .orElseThrow(() -> new IllegalArgumentException("팔로우 요청자 없음"));
    User toUser = userRepository.findById(toUserId)
        .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 없음"));

    return followRepository.existsByFromUserAndToUser(fromUser, toUser);
  }
}
