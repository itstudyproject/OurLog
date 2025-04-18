package com.example.ourLog.service;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Bid;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.BidRepository;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.TradeRepository;
import com.example.ourLog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;
  private final PictureRepository pictureRepository;
  private final UserRepository userRepository;
  private final BidRepository bidRepository;

  // 경매 등록
  @Override
  @Transactional
  public Trade bidRegist(TradeDTO dto) {
    Picture picture = pictureRepository.findById(dto.getPicId())
            .orElseThrow(() -> new RuntimeException("그림이 존재하지 않습니다."));
    User seller = userRepository.findById(dto.getSellerId())
            .orElseThrow(() -> new RuntimeException("판매자 정보가 존재하지 않습니다."));

    Trade trade = Trade.builder()
            .picId(picture)
            .sellerId(seller)
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

    if (dto.getBidAmount() % 1000 != 0) {
      throw new RuntimeException("입찰가는 1000원 단위로 입력해야 합니다.");
    }

    if (dto.getBidAmount() < trade.getHighestBid()) {
      throw new RuntimeException("입찰가는 현재 최고가보다 높아야 합니다.");
    }

    // 입찰가 갱신
    trade.setHighestBid(dto.getBidAmount());

    // 입찰자 정보 저장
    User bidder = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new RuntimeException("입찰자가 존재하지 않습니다."));

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
  public String nowBuy(Long tradeId, Long userId) {
    Trade trade = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("종료된 거래입니다.");
    }

    if (trade.getNowBuy() == null) {
      throw new RuntimeException("즉시 구매가가 설정되지 않은 상품입니다.");
    }

    User buyer = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자 정보가 존재하지 않습니다."));

    trade.setHighestBid(trade.getNowBuy());
    trade.setBidderId(buyer);
    trade.setTradeStatus(true);

    tradeRepository.save(trade);

    return "즉시구매가 완료되었습니다.";
  }


  // 경매 종료
  @Override
  @Transactional
  public String bidClose(Long tradeId, Long bidderId) {
    Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));
    User winner = userRepository.findById(bidderId)
            .orElseThrow(() -> new RuntimeException("낙찰자 정보가 존재하지 않습니다."));

    trade.setTradeStatus(true);
    trade.setBidderId(winner);

    tradeRepository.save(trade);
    return "경매가 종료되었습니다.";
  }

  // 마이페이지 - 낙찰 조회
  @Override
  public List<TradeDTO> getTrades(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자 정보가 존재하지 않습니다."));

    List<Trade> wonTrades = tradeRepository.findByBidderId(user);

    return wonTrades.stream()
            .map(trade -> TradeDTO.builder()
                    .tradeId(trade.getTradeId())
                    .picId(trade.getPicId().getPicId())
                    .picName(trade.getPicId().getPicName())
                    .startPrice(trade.getStartPrice())
                    .highestBid(trade.getHighestBid())
                    .nowBuy(trade.getNowBuy())
                    .bidderId(userId)
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
