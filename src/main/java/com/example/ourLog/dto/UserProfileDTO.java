package com.example.ourLog.dto;

import lombok.*;

import java.util.List;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

  private Long profileId; // UserProfile의 기본 키

  private User userId; // 유저 ID (profileId)
  private User nickname; // 닉네임 (User에서 추출)

  private String introduction; // 자기소개
  private String originImagePath; // 프로필 원본 이미지 경로
  private String thumbnailImagePath; // 프로필 썸네일 경로

  private Long followingCnt; // 팔로잉 수
  private Long followCnt; // 팔로우 수

  private List<TradeDTO> tradeList;

  private List<Trade> boughtList; // 구매 목록 (Trade ID만 리스트로)
  private List<Trade> soldList; // 판매 목록 (Trade ID만 리스트로)

  private Favorite isFavorited; // 북마크 ID 목록
  private Favorite favoritedPost; // 북마크된 게시물 ID 목록
}
