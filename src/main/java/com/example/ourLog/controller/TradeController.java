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
  private final PictureRepository pictureRepository;

  // 경매 조회
  @GetMapping("/picture/{pictureId}")
  public ResponseEntity<TradeDTO> getTradeByPictureId(@PathVariable Long pictureId) {
    Picture picture = pictureRepository.findById(pictureId)
        .orElseThrow(() -> new RuntimeException("해당 그림이 존재하지 않습니다."));

    Post post = picture.getPost();
    TradeDTO tradeDTO = tradeService.getTradeByPost(post);
    return ResponseEntity.ok(tradeDTO);
  }

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
      @RequestParam User user
  ) {
    String result = tradeService.nowBuy(tradeId, user);
    return ResponseEntity.ok(result);
  }

  // 랭킹(다운로드수)
  @GetMapping("/ranking")
  public ResponseEntity<?> getTradeRanking() {
    return ResponseEntity.ok(tradeService.getTradeRanking());
  }
}
