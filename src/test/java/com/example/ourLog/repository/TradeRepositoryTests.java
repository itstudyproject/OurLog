package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

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
  public void testInsertTrade() {
    // 테스트용으로 1번 사용자와 1번 게시글 가져오기
    Optional<User> userOpt = userRepository.findById(1L);
    Optional<Post> postOpt = postRepository.findById(1L);
    List<Picture> pictures = pictureRepository.findAll();

    assertTrue(userOpt.isPresent());
    assertTrue(postOpt.isPresent());
    assertFalse(pictures.isEmpty());

    User seller = userOpt.get();
    Post post = postOpt.get();
    Picture picture = pictures.get(0);

    Trade trade = Trade.builder()
        .post(post)
        .user(seller)
        .nowBuy(10000L)
        .tradeStatus(true) // 거래 완료된 것으로 설정
        .build();

    tradeRepository.save(trade);

    Optional<Trade> result = tradeRepository.findByPost(post);
    assertTrue(result.isPresent());
    System.out.println("Trade found: " + result.get());
  }

  @Test
  @Transactional
  public void testFindTradeRanking() {
    List<Object[]> ranking = tradeRepository.findTradeRanking();

    for (Object[] row : ranking) {
      Long picId = (Long) row[0];
      Long count = (Long) row[1];
      System.out.println("Picture ID: " + picId + ", 거래 횟수: " + count);
    }

    assertNotNull(ranking);
  }
}
