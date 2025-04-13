package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "trade")

public class Trade extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tradeId;

  @OneToOne
  @JoinColumn(name = "pic_id")
  private Picture picId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User sellerId;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "bidder_id")
  private User bidderId;

  private Boolean tradeStatus;



}
