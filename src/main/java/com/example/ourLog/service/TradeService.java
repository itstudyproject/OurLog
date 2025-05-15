package com.example.ourLog.service;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;

import java.util.List;
import java.util.Map;

public interface TradeService {

  // 경매 조회
  TradeDTO getTradeByPost(Post post);

  // 경매 등록
  Trade bidRegist(TradeDTO dto);

  // 입찰 갱신
  String bidUpdate(Long tradeId, TradeDTO dto);

  // 경매 종료
  String bidClose(Long tradeId);

  // 즉시 구매
  String nowBuy(Long tradeId, User user);

  // 마이페이지- 입찰목록
  List<TradeDTO> getTrades(User user);

  // 마이페이지- 구매목록
  List<TradeDTO> getPurchases(User user);

  // 마이페이지- 판매목록
  List<TradeDTO> getMySales(User user);

  // 마이페이지- 판매현황
  List<TradeDTO> getMySaleStatus(User user);

  List<TradeDTO> findByBuyer(User user);


  // 랭킹 (다운로드수)
  List<Map<String, Object>> getTradeRanking();
}
