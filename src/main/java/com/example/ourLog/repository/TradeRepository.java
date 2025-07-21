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

  // 판매자로 거래 조회
  List<Trade> findByUser_UserId(Long userId);

  // 판매자의 모든 경매 조회 (진행 중, 종료된 경매 포함)
  List<Trade> findByUser_UserIdOrderByRegDateDesc(Long userId);

  // 경매 조회
  Optional<Trade> findByPost(Post post);

  // 랭킹(다운로드수)
  @Query("SELECT p.postId, COUNT(t) " +
          "FROM Trade t " +
          "JOIN t.post p " +
          "WHERE t.tradeStatus = true " + // ✅ 거래 완료만 카운트
          "GROUP BY p.postId ")
  List<Object[]> findTradeRanking();
}