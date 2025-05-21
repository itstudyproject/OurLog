package com.example.ourLog.controller;

import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trades")
public class TradeController {

  private final TradeService tradeService;

  // 경매 등록
  @PostMapping("/register")
  public ResponseEntity<?> registerBid(
      @RequestBody TradeDTO dto,
      @AuthenticationPrincipal UserAuthDTO user
  ) {
    dto.setSellerId(user.getUserId()); // 유저 정보는 인증된 사용자로부터 받음
    Trade trade = tradeService.bidRegist(dto);
    return ResponseEntity.ok("경매등록 완료");
  }


  // 입찰
  @PostMapping("/{tradeId}/bid")
  public ResponseEntity<?> placeBid(
          @PathVariable Long tradeId,
          @RequestBody TradeDTO dto,
          @AuthenticationPrincipal UserAuthDTO currentBidder
  ) {
    String result = tradeService.bidUpdate(tradeId, dto, currentBidder);
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
      @AuthenticationPrincipal UserAuthDTO userAuthDTO // @AuthenticationPrincipal 사용
  ) {
    // UserAuthDTO 객체는 getUserId() 메소드를 가지고 있으므로 로그에서 사용 가능합니다.
    // User를 상속했으므로 getUsername()도 사용 가능합니다. 둘 중 편한 것을 사용하세요.

    // tradeService.nowBuy 메소드가 (Long tradeId, User user) 시그니처를 받는다고 가정할 때,
    // UserAuthDTO는 User를 상속하므로 userAuthDTO 객체 자체를 전달해도 타입 오류가 발생하지 않습니다.
    String result = tradeService.nowBuy(tradeId, userAuthDTO.getUserId()); // 이제 이 부분이 올바른 방식입니다.
    return ResponseEntity.ok(result);
  }

  // 랭킹(다운로드수)
  @GetMapping("/ranking")
  public ResponseEntity<?> getTradeRanking() {
    return ResponseEntity.ok(tradeService.getTradeRanking());
  }
}
