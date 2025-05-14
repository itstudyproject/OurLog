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

  private UserDTO user; // 유저 ID (profileId)

  private String introduction; // 자기소개
  private String originImagePath; // 프로필 원본 이미지 경로
  private String thumbnailImagePath; // 프로필 썸네일 경로

  private Follow follow; // 팔로잉 수

  private List<TradeDTO> boughtList; // 구매 목록 (TradeDTO로 변환)
  private List<TradeDTO> soldList; // 판매 목록 (TradeDTO로 변환)

  private List<FavoriteDTO> favorite; // 북마크 ID 목록 (FavoriteDTO로 변환)

}