package com.example.ourLog.controller;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {

  private final TradeService tradeService;

  // 경매 등록
  @PostMapping("/register")
  public ResponseEntity<?> registerBid(@RequestBody TradeDTO dto) {
    Trade trade = tradeService.bidRegist(dto);
    return ResponseEntity.ok( "경매등록 완료");
  }

  // 입찰
  @PostMapping("/{tradeId}/bid")
  public ResponseEntity<?> placeBid(
          @PathVariable Long tradeId,
          @RequestBody TradeDTO dto
  ) {
    String result = tradeService.bidUpdate(tradeId, dto);
    return ResponseEntity.ok(result);
  }

  // 경매 종료 및 낙찰자 설정
  @PutMapping("/{tradeId}/close")
  public ResponseEntity<?> closeTrade(
          @PathVariable Long tradeId
  ) {
    String result = tradeService.bidClose(tradeId);
    return ResponseEntity.ok(result);
  }

  // 즉시구매
  @PostMapping("/{tradeId}/nowBuy")
  public ResponseEntity<?> nowBuy(
      @PathVariable Long tradeId,
      @RequestParam User user
  ) {
    String result = tradeService.nowBuy(tradeId, user);
    return ResponseEntity.ok(result);
  }

  // 마이페이지 - 낙찰받은 그림 조회
  @GetMapping("/mypage")
  public ResponseEntity<List<TradeDTO>> getMyWonTrades(@RequestParam User user) {
    List<TradeDTO> result = tradeService.getTrades(user);
    return ResponseEntity.ok(result);
  }

  // 랭킹(다운로드수)
  @GetMapping("/ranking")
  public ResponseEntity<?> getTradeRanking() {
    return ResponseEntity.ok(tradeService.getTradeRanking());
  }
}
