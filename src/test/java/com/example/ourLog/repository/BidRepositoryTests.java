package com.example.ourLog.repository;

import com.example.ourLog.entity.Bid;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BidRepositoryTests {

  @Autowired
  TradeRepository tradeRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  BidRepository bidRepository;

  @Test
  @Transactional
  @Commit
  public void insertBidAndUpdateTradeTest() {
    Trade trade = tradeRepository.findById(1L).orElseThrow(); // 예시로 tradeId=1인 거래
    User bidder = userRepository.findById(5L).orElseThrow();

    Long bidAmount = 10000L;

    Bid bid = Bid.builder()
            .amount(bidAmount)
            .trade(trade)
            .user(bidder)
            .bidTime(LocalDateTime.now())
            .build();

    bidRepository.save(bid);

    // 🔁 Trade에 bidAmount, highestBid 갱신 (직접 해줘야 함)
    trade.setBidAmount(bidAmount);
    trade.setHighestBid(Math.max(trade.getHighestBid() == null ? 0 : trade.getHighestBid(), bidAmount));
    tradeRepository.save(trade);
  }


}