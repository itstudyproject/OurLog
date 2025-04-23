package com.example.ourLog.dto;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

  private Long profileId; // UserProfile의 기본 키

  private User user; // 유저 ID (profileId)

  private String introduction; // 자기소개
  private String originImagePath; // 프로필 원본 이미지 경로
  private String thumbnailImagePath; // 프로필 썸네일 경로

  private Follow follow; // 팔로잉 수
  private Long followCnt;
  private Long followingCnt;

  private List<Trade> boughtList; // 구매 목록 (Trade ID만 리스트로)
  private List<Trade> soldList; // 판매 목록 (Trade ID만 리스트로)

  private List<Favorite> favorite; // 북마크 ID 목록

}