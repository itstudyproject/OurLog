package com.example.ourLog.repository;

import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

  // 경매 조회
  Optional<Trade> findByPost(Post post);


  // 랭킹(다운로드수)
  @Query("SELECT p.postId, COUNT(t) " +
      "FROM Trade t " +
      "JOIN t.post p " + 
      "WHERE t.tradeStatus = true " +
      "GROUP BY p.postId")
  List<Object[]> findTradeRanking();
}