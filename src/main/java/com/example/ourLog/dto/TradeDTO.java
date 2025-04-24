package com.example.ourLog.dto;

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
  private PictureDTO pictureDTO;
  private String picName;

  // 사용자 정보
  private UserDTO userDTO; // 요청자 (등록자 or 입찰자)
  private Long seller;   // 판매자

  // 가격 정보
  private Long startPrice;   // 경매 시작가
  private Long highestBid;   // 최고 입찰가
  private Long bidAmount;    // 입찰 금액
  private Long nowBuy; // 즉시구매
  // 상태
  private boolean tradeStatus; // 거래현황 true = 종료됨

  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
