package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeRepositoryTests {

  @Autowired
  UserRepository userRepository;

  @Autowired
  TradeRepository tradeRepository;

  @Autowired
  PictureRepository pictureRepository;

  @Test
  @Transactional
  @Commit
  public void insertTradeTest() {
    IntStream.rangeClosed(1, 50).forEach(i -> {
      Long userId = (long) (i % 20 + 1); // 1~20번 사용자만 사용
      Long pictureId = (long) (i + 100); // 100~149번 그림 (중복 피하기)

      boolean isDone = i % 2 == 0; // 짝수번째는 입찰 완료, 홀수는 진행 중
      Long startPrice = 10000L + i * 1000;
      Long highestBid = isDone ? startPrice + 5000 : 0L;
      Long nowBuy = startPrice + 10000;

      Trade trade = Trade.builder()
              .startPrice(startPrice)
              .highestBid(highestBid)
              .nowBuy(nowBuy)
              .tradeStatus(!isDone) // true: 진행중, false: 완료
              .user(User.builder().userId(userId).build())
              .picture(Picture.builder().picId(pictureId).build())
              .build();

      tradeRepository.save(trade);
    });
  }
}