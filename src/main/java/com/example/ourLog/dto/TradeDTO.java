package com.example.ourLog.dto;

import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeDTO {

  // 거래 정보
  private Long tradeId;
  private Long postId;
  private Long sellerId; // 요청자 (등록자)
  private Long bidderId; // 요청자 (입찰자)
  private String bidderNickname; // 입찰자 닉네임 추가

  // 가격 정보
  private Long startPrice;   // 경매 시작가
  private Long highestBid;   // 최고 입찰가
  private Long bidAmount;    // 입찰 금액
  private Long nowBuy; // 즉시구매
  // 상태
  private boolean tradeStatus; // 거래현황 true = 종료됨

  private LocalDateTime regDate;
  private LocalDateTime modDate;
  private LocalDateTime lastBidTime; // 마지막 입찰 시간 추가
}
