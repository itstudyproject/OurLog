package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

  // 경매 조회
  Optional<Trade> findByPicId(Picture picture);

  // 낙찰 목록 조회 (마이페이지)
  List<Trade> findByBidderId(User bidder);

  @Query ("SELECT t.picId.picId, COUNT(t) FROM Trade t WHERE t.tradeStatus = true GROUP BY t.picId")
  List<Object[]> findTradeRanking();
}