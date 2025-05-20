package com.example.ourLog.repository;

import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

  // 1. 팔로우 관계가 존재하는지 확인 (exists 사용)
  @Query("select case when exists (select 1 from Follow fo where fo.fromUser = :fromUser and fo.toUser = :toUser) then true else false end")
  boolean existsByFromUserAndToUser(User fromUser, User toUser);

  // 2. 팔로우 관계 삭제 (fromUser와 toUser로 팔로우 관계 삭제)
  @Query("delete from Follow fo where fo.fromUser = :fromUser and fo.toUser = :toUser")
  void deleteByFromUserAndToUser(User fromUser, User toUser);

  // 3. 팔로우 요청자가 팔로우한 모든 사람들 조회
  @Query("select fo from Follow fo where fo.fromUser = :fromUser")
  List<Follow> findAllByFromUser(User fromUser);

  // 4. 팔로워가 팔로우하는 모든 사람들 조회
  @Query("select fo from Follow fo where fo.toUser = :toUser")
  List<Follow> findAllByToUser(User toUser);

  // 5. 특정 userId를 팔로우한 사용자 목록 조회
  @Query("select fo.fromUser from Follow fo where fo.toUser.userId = :userId")
  List<User> findFollowersByUserId(@Param("userId") Long userId); // Long으로 userId 전달


  Long countByToUser(User toUser);

  Long countByFromUser(User fromUser);

}
