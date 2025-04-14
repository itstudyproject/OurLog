package com.example.ourLog.controller;

<<<<<<< Updated upstream
public class TradeController {
=======
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Trade;
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

  // 1. 경매 등록
  @PostMapping("/register")
  public ResponseEntity<?> registerBid(@RequestBody TradeDTO dto) {
    Trade trade = tradeService.bidRegist(dto);
    return ResponseEntity.ok( "경매등록 완료");
  }

  // 2. 입찰
  @PostMapping("/{tradeId}/bid")
  public ResponseEntity<?> placeBid(
          @PathVariable Long tradeId,
          @RequestBody TradeDTO dto
  ) {
    String result = tradeService.bidUpdate(tradeId, dto);
    return ResponseEntity.ok(result);
  }

  // 3. 경매 종료 및 낙찰자 설정
  @PutMapping("/{tradeId}/close")
  public ResponseEntity<?> closeTrade(
          @PathVariable Long tradeId,
          @RequestParam Long bidderId
  ) {
    String result = tradeService.bidClose(tradeId, bidderId);
    return ResponseEntity.ok(result);
  }

  // 4. 마이페이지 - 낙찰받은 그림 조회
  @GetMapping("/mypage")
  public ResponseEntity<List<TradeDTO>> getMyWonTrades(@RequestParam Long userId) {
    List<TradeDTO> result = tradeService.getTrades(userId);
    return ResponseEntity.ok(result);
  }
>>>>>>> Stashed changes
}
