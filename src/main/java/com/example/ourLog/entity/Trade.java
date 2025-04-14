package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trade")
public class Trade extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tradeId; // 거래 번호

  private Long startPrice; // 경매 시작가
  private Long highestBid; // 최고 입찰가

  private boolean tradeStatus; // 거래 현황

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pic_id")
  private Picture picId; // 그림 번호

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User sellerId; // 판매자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bidder_id")
  private User bidderId; // 낙찰자




}