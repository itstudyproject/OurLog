package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeRepositoryTests {

  @Autowired
  private TradeRepository tradeRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private PictureRepository pictureRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @Transactional
  @Commit
  public void insertTradeTest() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      Long userId = (long) (i % 100 + 1);
      Long postId = (long) (i);

      boolean isCompleted = i % 2 == 0;        // 짝수는 완료된 거래, 홀수는 진행 중

      Long startPrice = 10000L + i * 1000;
      Long nowBuy = 20000L + i * 1000;
      Long highestBid = isCompleted
              ? nowBuy // 완료된 경우는 즉시 구매가로 낙찰
              : 0L;     // 진행 중이면 최고 입찰가 없음

      Trade trade = Trade.builder()
              .startPrice(startPrice)
              .highestBid(highestBid)
              .nowBuy(nowBuy)
              .tradeStatus(!isCompleted) // true: 진행 중, false: 완료
              .user(User.builder().userId(userId).build())
              .post(Post.builder().postId(postId).build())
              .build();

      tradeRepository.save(trade);
    });
  }




  @Test
  @Transactional
  public void testFindTradeRanking() {
    List<Object[]> ranking = tradeRepository.findTradeRanking();

    for (Object[] row : ranking) {
      Post post = (Post) row[1];
      Long count = (Long) row[1];
      System.out.println("거래번호: " + post.getPostId() + ", 거래 횟수: " + count);
    }

    assertNotNull(ranking);
  }
}
