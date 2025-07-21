package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bidId;

  // 입찰 금액
  private Long amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trade_id")
  private Trade trade;

  // 입찰자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bidder_id")
  private User user;

  // 입찰한 시간
  private LocalDateTime bidTime;
}
