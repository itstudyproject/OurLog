package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.*;
import com.example.ourLog.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final BidRepository bidRepository;

  // 경매 조회
  @Override
  @Transactional
  public TradeDTO getTradeByPost(Post post) {
    Trade trade = tradeRepository.findByPost(post)
            .orElseThrow(() -> new RuntimeException("관련된 거래가 존재하지 않습니다."));

    return TradeDTO.builder()
            .tradeId(trade.getTradeId())
            .postId(trade.getPost().getPostId())
            .startPrice(trade.getStartPrice())
            .highestBid(trade.getHighestBid())
            .nowBuy(trade.getNowBuy())
            .tradeStatus(trade.isTradeStatus())
            .regDate(trade.getRegDate())
            .modDate(trade.getModDate())
            .build();
  }

  @Override
  public List<TradeDTO> getTradeByUserId(Long userId) {
    List<Trade> trades = tradeRepository.findByUser_UserId(userId);
    
    if (trades.isEmpty()) {
      throw new RuntimeException("해당 사용자의 거래 내역이 없습니다");
    }

    return trades.stream()
            .map(trade -> TradeDTO.builder()
                    .tradeId(trade.getTradeId())
                    .postId(trade.getPost().getPostId())
                    .sellerId(trade.getUser().getUserId())
                    .startPrice(trade.getStartPrice())
                    .highestBid(trade.getHighestBid())
                    .nowBuy(trade.getNowBuy())
                    .tradeStatus(trade.isTradeStatus())
                    .build())
            .collect(Collectors.toList());
  }

  // 경매 등록
  @Override
  @Transactional
  public Trade bidRegist(TradeDTO dto) {
    Post post = postRepository.findById(dto.getPostId())
            .orElseThrow(() -> new RuntimeException("그림이 존재하지 않습니다."));
    User seller = userRepository.findById(dto.getSellerId())
            .orElseThrow(() -> new RuntimeException("판매자가 존재하지 않습니다."));


    Trade trade = Trade.builder()
            .post(post)
            .user(seller)
            .startPrice(dto.getStartPrice())
            .highestBid(dto.getStartPrice()) // 시작가는 최고입찰가로 초기화
            .tradeStatus(false)
            .build();

    return tradeRepository.save(trade);
  }

  // 입찰 갱신
  @Override
  @Transactional
  public String bidUpdate(Long tradeId, TradeDTO dto) {
    Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
        throw new RuntimeException("종료된 경매입니다.");
    }

    // 입찰가 기본 검증
    if (dto.getBidAmount() == null || dto.getBidAmount() <= 0) {
        throw new RuntimeException("올바른 입찰 금액을 입력해주세요.");
    }

    if (dto.getBidAmount() % 1000 != 0) {
        throw new RuntimeException("입찰가는 1000원 단위로 입력해야 합니다.");
    }

    // 최소 입찰가 검증
    Long minBidAmount = trade.getHighestBid() + 1000; // 최소 1000원 이상 높게
    if (dto.getBidAmount() < minBidAmount) {
        throw new RuntimeException("입찰가는 현재 최고가보다 1000원 이상 높아야 합니다.");
    }

    // 즉시 구매가 검증
    if (trade.getNowBuy() != null && dto.getBidAmount() >= trade.getNowBuy()) {
        throw new RuntimeException("즉시 구매가 이상으로 입찰할 수 없습니다. 즉시 구매를 이용해주세요.");
    }

    // 입찰자 정보
    User bidder = userRepository.findById(dto.getBidderId())
            .orElseThrow(() -> new RuntimeException("입찰자가 존재하지 않습니다."));

    // 판매자 본인 입찰 방지
    if (bidder.getUserId().equals(trade.getUser().getUserId())) {
        throw new RuntimeException("판매자는 본인의 경매에 입찰할 수 없습니다.");
    }

    // 최고 입찰가 갱신
    trade.setHighestBid(dto.getBidAmount());

    Bid bid = Bid.builder()
            .amount(dto.getBidAmount())
            .trade(trade)
            .user(bidder)
            .bidTime(LocalDateTime.now())
            .build();

    bidRepository.save(bid);
    tradeRepository.save(trade);

    return "입찰이 등록되었습니다.";
  }

  // 즉시 구매
  @Override
  @Transactional
  public String nowBuy(Long tradeId, User user) {
    Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("종료된 거래입니다.");
    }

    if (trade.getNowBuy() == null) {
      throw new RuntimeException("즉시 구매가가 설정되지 않은 상품입니다.");
    }

    User buyer = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new RuntimeException("사용자 정보가 존재하지 않습니다."));

    Bid bid = Bid.builder()
            .amount(trade.getNowBuy())
            .trade(trade)
            .user(buyer)
            .bidTime(LocalDateTime.now())
            .build();
    bidRepository.save(bid);

    trade.setHighestBid(trade.getNowBuy());
    trade.setTradeStatus(true);
    tradeRepository.save(trade);

    return "즉시구매가 완료되었습니다.";
  }


  // 경매 종료
  @Override
  @Transactional
  public String bidClose(Long tradeId) {
    Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("이미 종료된 거래입니다.");
    }

    // 낙찰자 조회
    Optional<Bid> winningBidOpt = bidRepository.findTopByTradeAndAmount(trade, trade.getHighestBid());
    if (winningBidOpt.isEmpty()) {
      throw new RuntimeException("낙찰자가 존재하지 않습니다.");
    }

    // 거래 종료
    trade.setTradeStatus(true);
    tradeRepository.save(trade);

    return "경매가 종료되었습니다."; // 낙찰자 정보는 bid 테이블에서 확인 가능
  }


  // 마이페이지 - 낙찰 조회
  @Override
  public List<TradeDTO> getTrades(User user) {
    List<Trade> wonTrades = bidRepository.findWonTradesByUser(user);

    return wonTrades.stream()
            .map(trade -> TradeDTO.builder()
                    .tradeId(trade.getTradeId())
                    .postId(trade.getPost().getPostId())
                    .startPrice(trade.getStartPrice())
                    .highestBid(trade.getHighestBid())
                    .nowBuy(trade.getNowBuy())
                    .tradeStatus(trade.isTradeStatus())
                    .build())
            .collect(Collectors.toList());
  }

  // 랭킹(다운로드수)
  @Override
  public List<Map<String, Object>> getTradeRanking() {
    List<Object[]> result = tradeRepository.findTradeRanking();

    return result.stream().map(row -> {
      Map<String, Object> map = new HashMap<>();
      map.put("picId", row[0]);
      map.put("tradeCount", row[1]);
      return map;
    }).sorted((a, b) ->
            ((Long) b.get("tradeCount")).compareTo((Long) a.get("tradeCount"))
    ).collect(Collectors.toList());
  }
}
