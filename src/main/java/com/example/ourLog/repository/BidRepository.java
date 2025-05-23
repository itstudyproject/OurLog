package com.example.ourLog.repository;

import com.example.ourLog.entity.Bid;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
  // 사용자 ID로 현재 입찰 중인 경매 조회
  @Query("SELECT DISTINCT b.trade FROM Bid b " +
      "JOIN b.trade t " +
      "WHERE b.user.userId = :userId " +
      "AND t.tradeStatus = false " +
      "AND b.amount = t.highestBid")List<Trade> findCurrentBidTradesByUserId(@Param("userId") Long userId);
  List<Bid> findByTrade(Trade trade);

  List<Bid> findByUser(User user);

  // 최근 입찰 정보 조회
  Optional<Bid> findTopByTradeOrderByBidTimeDesc(Trade trade);

  // 낙찰자(최고입찰자) 찾는 쿼리문
  @Query("SELECT b FROM Bid b WHERE b.trade = " +
      ":trade AND b.amount = " +
      ":amount ORDER BY b.bidTime ASC")
  Optional<Bid> findTopByTradeAndAmount(@Param("trade") Trade trade, @Param("amount") Long amount);

  // 사용자 ID로 낙찰받은 경매 조회
  @Query("SELECT DISTINCT b.trade FROM Bid b " +
      "JOIN b.trade t " +
      "WHERE b.user.userId = :userId " +
      "AND t.tradeStatus = true " + // 종료된 경매만
      "AND b.amount = t.highestBid")
  List<Trade> findWonTradesByUserId(@Param("userId") Long userId);

}
