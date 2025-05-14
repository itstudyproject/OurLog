package com.example.ourLog.entity;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserDTO;
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
  private Long bidAmount;    // 입찰 금액
  private Long nowBuy; // 즉시 구매
  private boolean tradeStatus; // 거래 현황

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  @JsonProperty
  private Post post; // 게시글 하나에 포함된 그림 거래

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  @JsonProperty
  private User user; // 판매자

  @Builder.Default
  @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bid> bidHistory = new ArrayList<>();

  // Trade.java
  private LocalDateTime createdAt;

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }


}
