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
  List<Bid> findByTrade(Trade trade);

  List<Bid> findByUser(User user);

  // 낙찰자(최고입찰자) 찾는 쿼리문
  @Query("SELECT b FROM Bid b WHERE b.trade = " +
      ":trade AND b.amount = " +
      ":amount ORDER BY b.bidTime ASC")
  Optional<Bid> findTopByTradeAndAmount(@Param("trade") Trade trade, @Param("amount") Long amount);

}
