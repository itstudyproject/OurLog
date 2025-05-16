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

  TradeDTO getTradeByUserId(Long userId);

  // 경매 등록
  Trade bidRegist(TradeDTO dto);

  // 입찰 갱신
  String bidUpdate(Long tradeId, TradeDTO dto);

  // 경매 종료
  String bidClose(Long tradeId);

  // 즉시 구매
  String nowBuy(Long tradeId, User user);

  // 낙찰 목록 조회
  List<TradeDTO> getTrades(User user);

  // 랭킹 (다운로드수)
  List<Map<String, Object>> getTradeRanking();
}
