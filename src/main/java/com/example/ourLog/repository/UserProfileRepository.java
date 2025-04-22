package com.example.ourLog.repository;

import com.example.ourLog.entity.UserProfile;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  // 1. profileId.userId로 조회
  @EntityGraph(attributePaths = {"profileId", "nickname"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select up from UserProfile up where up.profileId.userId = :userId")
  Optional<UserProfile> findByProfileId_Id(@Param("userId") Long userId);

  // 2. 닉네임으로 조회
  @EntityGraph(attributePaths = {"nickname"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select up from UserProfile up where up.nickname = :nickname")
  Optional<UserProfile> findByNickname(@Param("nickname") String nickname);

  // 3. 닉네임 키워드 검색
  @EntityGraph(attributePaths = {"nickname"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select up from UserProfile up where up.nickname like %:keyword%")
  List<UserProfile> searchByNickname(@Param("keyword") String keyword);

  // 4. userId로 삭제
  @Modifying
  @Query("delete from UserProfile up where up.profileId.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}

