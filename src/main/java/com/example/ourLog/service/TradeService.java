package com.example.ourLog.service;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import com.example.ourLog.security.dto.UserAuthDTO;

import java.util.List;
import java.util.Map;

public interface TradeService {

  // 경매 조회
  TradeDTO getTradeByPost(Post post);

  // 사용자 ID로 거래 내역 조회
  List<TradeDTO> getTradeByUserId(Long userId);

  // 경매 등록
  Trade bidRegist(TradeDTO dto);

  // 입찰 갱신
  String bidUpdate(Long tradeId, TradeDTO dto, UserAuthDTO currentBidder);

  // 경매 종료
  String bidClose(Long tradeId);

  // 즉시 구매
  String nowBuy(Long tradeId, User user);

  // 구매 목록 조회 (현재 입찰 중, 낙찰받은 목록)
  Map<String, List<TradeDTO>> getPurchaseList(Long userId);

  // 랭킹 (다운로드수)
  List<Map<String, Object>> getTradeRanking();

  // 판매 목록 조회 (진행 중, 종료된 경매 포함)
  List<TradeDTO> getSalesList(Long userId);
}
