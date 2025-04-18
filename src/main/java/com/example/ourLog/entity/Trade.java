package com.example.ourLog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
  private Long nowBuy; // 즉시 구매

  private boolean tradeStatus; // 거래 현황

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pic_id")
  @JsonProperty
  private Picture picId; // 그림 번호

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  @JsonProperty
  private User user; // 판매자

  @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bid> bidHistory = new ArrayList<>();
}
