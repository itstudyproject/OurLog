package com.example.ourLog.service;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Trade;

import java.util.List;
import java.util.Map;

public interface TradeService {

  // 경매 등록
  Trade bidRegist(TradeDTO dto);

  // 입찰 갱신
  String bidUpdate(Long tradeId, TradeDTO dto);

  // 경매 종료
  String bidClose(Long tradeId, Long bidderId);

  // 즉시 구매
  String nowBuy(Long tradeId, Long userId);

  // 낙찰 목록 조회
  List<TradeDTO> getTrades(Long userId);

  // 랭킹 (다운로드수)
  List<Map<String, Object>> getTradeRanking();
}
