package com.example.ourLog.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

  private Long id; // UserProfile의 기본 키

  private Long userId; // 유저 ID (profileId)
  private String nickname; // 닉네임 (User에서 추출)

  private String introduction; // 자기소개
  private String originImagePath; // 프로필 원본 이미지 경로
  private String thumbnailImagePath; // 프로필 썸네일 경로

  private Long followingCnt; // 팔로잉 수
  private Long followCnt; // 팔로우 수

  private List<Long> boughtListIds; // 구매 목록 (Trade ID만 리스트로)
  private List<Long> soldListIds; // 판매 목록 (Trade ID만 리스트로)

  private List<Long> favoriteIds; // 북마크 ID 목록
  private List<Long> favoritePostIds; // 북마크된 게시물 ID 목록
}
